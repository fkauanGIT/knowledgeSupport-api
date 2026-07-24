# knowledgeSupport-api limitations

> This document exists so the pretty architecture doesn't disguise a domain problem.
> Read it together with [ARCHITECTURE.md](ARCHITECTURE.md).

## The core problem: matching still doesn't understand synonyms, just similar-looking text

**Updated (phase 1.1–1.3 of `BACKLOG.md`):** the matcher no longer requires exact string
equality. `AnalyzeCalledService.analyze` now tries, in cascade:

1. Exact match: same `routineNumber` + normalized `errorName`/`standardName` (no
   accents/case/duplicate spaces, via `TextSimilarity.normalize`) — the same behavior as
   before, just tolerant of trivial typing differences.
2. Text similarity score (`TextSimilarity.score`): tokenizes
   `titleCalled+descriptionCalled+errorName` against `standardName+text`, strips Portuguese
   stopwords (preserving negation — "não" is never discarded) and measures **containment**
   (how much of the ticket is covered by the Standard, not the other way around) with typo
   tolerance (Levenshtein). Asymmetric on purpose: a rich Standard, covering several ways of
   describing the same bug, shouldn't be penalized for having more text than the ticket.
   `routineNumber` became a **filter that prioritizes candidates**, no longer a mandatory
   pair: a ticket with no routine filled in, or with the wrong routine, can still find a
   Standard by text.
3. A configurable threshold (`matching.threshold`, default 0.4) decides what counts as a
   match; the score is visible in `MatchMethod` and in the API response
   (`CalledAnalysisResponse.score`).

This already solves the case of trivial spelling divergence ("Caixa não fecha" vs. "caixa nao
fecha") and typos (Levenshtein). **It does not solve** the harder case, which is still the
real-world N3 scenario:

- The containment score measures **word overlap**, not meaning. *"I closed a register
  yesterday and it's still showing as open"* and *"register stuck open after closing"*
  share few exact words (or Levenshtein-close ones) despite being the same problem — only a
  semantic stage (embeddings, backlog item 1.5, not implemented yet) would solve that. It's
  even worse when the root cause was never written in the original ticket (e.g. a UI symptom
  that only turns out to be connected to a tax-configuration problem after investigation) —
  not even embeddings would solve that, because the root-cause keyword simply doesn't exist
  in either side's text until someone figures it out and registers it.
- `Standard.result` is still the single "final answer" applied by the matcher — but a
  `Standard` can now also carry `investigationSteps` (item 1.6, done), recording the
  hypotheses tested, the query used to check each one, and what confirmed or discarded it.
  This is a **documentation trail, not a smarter matcher**: `CalledStandardMatcher` still
  only scores `standardName`+`text` and still gates on `result` being present — a Standard
  with zero steps behaves exactly as before, on purpose (see below).
- The Standards knowledge base is still scanned entirely in memory (`findAll()`); Postgres
  full-text search (item 1.4) only comes in once the volume justifies it.

**Practical consequence:** the system today handles light paraphrasing and typos well, but
still depends on shared vocabulary between whoever opened the ticket and whoever registered
the Standard. Two reports using completely different vocabulary for the same real bug still
won't find each other.

**Update (confidence level on the analysis response):** the risk this created in practice
wasn't just "no match" — it was a match that *looks* certain when it isn't. A ticket with
vague or unrelated text could still clear `matching.threshold` and come back as a plain
`solution`, indistinguishable in the response from an exact match. `MatchMethod` now carries
an explicit `Confidence` (`CONFIRMED` for exact match, `LIKELY`/`UNCERTAIN` for score-based
matches split by `matching.high-confidence-threshold`, `NONE` for no match), exposed in
`CalledAnalysisResponse.confidence`. **This makes the existing uncertainty visible instead of
silent — it does not make the matching smarter.** `UNCERTAIN` is still the same containment
score as before, still word-overlap, still capable of being the wrong Standard; the only
change is that the analyst now sees an explicit "this is a guess" flag instead of having to
infer it from a raw float. The semantic gap described above is untouched by this change.

Swapping databases, swapping Jira, adding Chatwoot — none of that fixes this. Hexagonal
architecture protects the core from infrastructure changes; it doesn't fix a malformed
business rule. The defect is inside the hexagon, not at the boundary.

## Other known limitations (not the focus of this document, but real)

| Limitation | Where | Impact |
|---|---|---|
| Authentication is just a static shared key, no users/roles | `adapter/in/web/security` (`ApiKeyAuthFilter`) | Fine for machine-to-machine (Jira webhook, internal calls) with no login; if a frontend with real users shows up (item 2.6), this needs to become something with per-user identity (e.g. JWT) |
| `Called` is never persisted, always fetched live from Jira | `CalledProviderPort` / `JiraCalledAdapter` | Paginated (`nextPageToken`) and retries on 429; now also **timeouts** (2s connect / 8s read) and a short **read-through cache** (Caffeine `openCalleds`, TTL 45s, keyed by JQL+filter). Jira stays the source of truth — staleness is bounded to the TTL, not eliminated, and there's still no local persistence/history. |
| Text score is word overlap, not semantics | `TextSimilarity.score` | A distant synonym/paraphrase ("register won't close" vs. "cashier can't close out the day") still doesn't match — needs item 1.5 (embeddings) |
| **Similar-but-different problems compete for the same routine** | `TextSimilarity.score` / `CalledStandardMatcher` | The containment score measures word overlap, not the *nature* of the problem. Two genuinely different issues that happen to share the same routine and some vocabulary can tie or even have the wrong one win — e.g. a registered fix like "press F5 and retry" (a system glitch) versus a ticket phrased as "can't make a sale on routine 4116" that on investigation turns out to be "I don't know how to complete a sale" (a training gap, not a bug). The score has no dimension for *type of cause* (bug vs. lack of training vs. configuration), only for word overlap within a routine. Today the safety net is the feedback loop (`FeedbackService`/accuracy, item 2.4): if a Standard keeps getting suggested for the wrong kind of ticket, its accuracy rate drops and signals that a more specific Standard is missing. A structural fix — tagging Standards with a "cause type" as a second filter alongside `routineNumber` — is a business decision (which field, whose call) that hasn't been made; see the `FilterCategory` limitation below for the same kind of open question. |
| `FilterCategory` is still fixed at `SUPPORT` | `JiraCalledMapper.toDomain` | There's no reliable field in Jira today to tell SUPPORT/INFRASTRUCTURE/DEVELOPMENT apart, and every ticket that reaches this system today is support anyway — defaults to `SUPPORT` until a real field exists to derive the others, would need a new business field, not just reading one more field off the issue |
| `GapReportService`/`FeedbackService` have no integration tests | `GapReportServiceTest`/`FeedbackServiceTest` cover the logic with mocked ports | The real Postgres query (`findByStandardId`) and real Jira pagination aren't exercised in tests |

| `DemoApplicationTests.contextLoads` needs a real datasource | `@SpringBootTest` boots the full context | `mvn test` fails with `'url' must start with "jdbc"` unless `DB_URL` (and the other `.env` vars) are set — the smoke test spins up Hikari/JPA against the configured DB. Makes CI depend on a reachable database; a dedicated test profile (H2 in-memory or Testcontainers) would decouple it. |

## What would need to change to support an N3 flow

This is a business-rule redesign inside the core, not a new adapter:

1. ~~Replace exact equality on `errorName` with text search over `descriptionCalled` +
   `Standard.text` (keeping `routineNumber` as a filter, not a mandatory pair).~~ **Done** —
   fuzzy matching via containment score + Levenshtein, see the section above. Still missing
   the semantic step (embeddings) for distant paraphrasing — and even that step wouldn't fix
   a root cause that was never written down in the original ticket (see the example above).
2. ~~Model `Standard` as a sequence of investigation steps (tables, fields, queries,
   discarded hypotheses), not a single `result` field.~~ **Done** — `investigationSteps` on
   `Standard` (`InvestigationStep`: hypothesis/query/verification/confirmed), see
   `ARCHITECTURE.md`. Deliberately incomplete on two fronts, by choice: (a) matching doesn't
   read or gate on the steps at all — it's context for a human, not a new score dimension;
   (b) there's still no peer review of what gets registered — today only one person
   authors and consults the knowledge base, so a review workflow would be solving a problem
   that doesn't exist yet. Revisit both once there's real usage data (steps recorded on
   existing Standards) or a second analyst in the loop.
3. A semantic layer (embeddings) as the last step of the cascade, never replacing the
   deterministic methods above (backlog item 1.5, optional/future).

With items 1 and 2 done, the system handles light paraphrasing and typos, and a registered
solution now carries the reasoning behind it instead of a bare answer — but it still serves
semi-deterministic errors better than root-cause N3 diagnosis with fully free vocabulary.
Item 3 (and, longer-term, peer review once more than one analyst is involved) is what's
still missing for that to really change.
