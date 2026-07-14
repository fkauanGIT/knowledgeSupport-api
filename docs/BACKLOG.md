# Backlog — knowledgeSupport-api

> Dual-purpose document: (1) a record of what's left to do, so nothing gets lost; (2) a **ready-to-paste
> prompt for an AI** when working on any item. Copy from the top down through the end of the
> "Context for AI" block + whichever item you're implementing.

---

## Context for AI (paste this at the start of any session)

You're going to work on **knowledgeSupport-api**: a Java 17 + Spring Boot (4.x, Maven) API in
**Hexagonal Architecture**. The system integrates Jira tickets (project SUP, request type
"Errors/Alerts in the WINTHOR system"), maintains a catalog of error patterns with solutions
(`Standard`, PostgreSQL), and analyzes tickets to suggest the registered solution
(`GET /api/calleds/{key}/analysis`).

**Non-negotiable project rules:**

1. **Dependency rule**: `adapter → application → domain`. `domain` and `application`
   import NOTHING from web/JPA/Jackson/HTTP (exception: Spring's `@Service` annotations).
   External formats (Jira JSON, JPA entities) die in the adapters' mappers.
2. **Ports**: a new capability the core needs = interface in `application/port/out` +
   adapter. A new service the core offers = interface in `application/port/in` + service.
   A new provider for an existing capability = just a new adapter.
3. **No new dependency without justifying it** — and never another language in the repo.
4. **Secrets only in `.env`** (gitignored). Never in code, a committed yaml, or docs.
5. **Conventional Commits** (`feat:`/`fix:`/`docs:`/`chore:`) — versioning is automatic
   (Release Please). Work on a branch, PR to main with squash merge.
6. Document new decisions in `docs/ARCHITECTURE.md` (decision table) and keep
   `docs/LIMITATIONS.md` honest.
7. Endpoints documented with springdoc annotations **only in `adapter/in/web`**.
8. Read before coding: `docs/ARCHITECTURE.md`, `docs/FOLDER_STRUCTURE.md`,
   `docs/LIMITATIONS.md`.

**Current state:** Standards CRUD with `routineNumber` and accuracy rate from feedback ·
Jira tickets with routine, error name, jiraKey and status (paginated, retries on 429) ·
cascading analysis (exact match → containment score+Levenshtein, `routineNumber` as a
filter) · gap report by routine · 404 handled centrally · Swagger ·
API key authentication · automatic versioning.

---

## PHASE 1 — Matching that actually works (fixes LIMITATIONS.md)

- [x] **1.1 Normalize the current comparison** — lowercase, no accents
      (`java.text.Normalizer`), trim and collapse whitespace before comparing `errorName`
      × `standardName`. Quick win; eliminates silly case/accent typo failures.
- [x] **1.2 Text matching with a score** — tokenize `titleCalled + descriptionCalled +
      errorName` from the ticket and `standardName + text` from the Standard; strip
      Portuguese stopwords; Jaccard similarity; `routineNumber` becomes a **filter** (reduces
      candidates), not a mandatory pair. Result carries a 0–1 score with a configurable
      threshold in the yaml. `MatchMethod` gains a score/confidence in the response.
      **Implementation note:** symmetric Jaccard (intersection/union) penalizes a rich
      Standard — the more symptom variations a Standard accumulates, the worse its score
      gets, the opposite of what we want. Replaced with *containment* (intersection/ticket
      tokens), asymmetric on purpose. See `docs/ARCHITECTURE.md` (decision table).
- [x] **1.3 Typo tolerance** — Levenshtein via Apache Commons Text for token comparison
      (the only new dependency allowed in this phase).
- [ ] **1.4 (once the knowledge base grows) PostgreSQL full-text search** —
      tsvector/tsquery via a port (`StandardSearchPort` or a method on the RepositoryPort) to
      avoid scanning everything in memory.
- [ ] **1.5 (future, optional) Semantic layer** — a semantic matching port
      (embeddings/AI) as ONE MORE step in the cascade, never replacing the
      deterministic ones. AI = "nice-sounding answers"/last resort, not a dependency.
- [x] **1.6 Remodel `Standard` for an N3 flow** — `investigationSteps` (list of
      `InvestigationStep`: hypothesis, query, verification, confirmed) added alongside the
      existing `result`. Documentation/reasoning trail only: matching still scores
      `standardName`+`text` and gates on `result`, unaffected by steps being present, empty,
      or absent — no gating on `confirmed`, no peer review added (still one analyst
      authoring today, by choice — see `LIMITATIONS.md`).

## PHASE 2 — Visible value (product)

- [x] **2.1 `jiraKey` (and status) on `Called`** — a prerequisite for the items below:
      referencing the ticket at its source. Also map the real status (statusCategory).
      **Note:** the status *name* is mapped (`fields.status().name()`); `statusCategory`
      (new/indeterminate/done) wasn't persisted as its own field — there was no defined use
      for it yet; add it when a concrete need shows up.
- [ ] **2.2 Automatic Jira comment** — when the analysis finds a Standard, post a
      comment on the ticket with the solution (port `TicketCommentPort` + a write in
      `JiraAdapter`). The feature that makes the team SEE the system working.
- [x] **2.3 Gap report** — `GET /api/calleds/gap-report` aggregates analyses with
      no match, grouped by routine and frequency: "register these patterns and cover X% of
      the volume". Drives feeding the knowledge base.
- [x] **2.4 Resolved/not-resolved feedback** — `POST /api/calleds/{key}/feedback` +
      `GET /api/standards/{id}/accuracy` — accuracy rate per Standard (auditable
      confidence, something AI doesn't give you).
- [ ] **2.5 Chatwoot** — `adapter/in/chatwoot` (conversation webhook) and
      `adapter/out/chatwoot` (reply to the requester).
- [ ] **2.6 Web management interface** — a separate frontend consuming the API (Standards +
      analyses + reports).

## PHASE 3 — Robustness and quality

- [x] **3.1 Correct HTTP errors** — `GlobalExceptionHandler` (`@RestControllerAdvice`):
      nonexistent ticket/Standard → 404 with an explanatory body, centralized (previously
      each controller repeated the same catch).
- [x] **3.2 Authentication** — Spring Security with an API key (`X-API-KEY` header,
      validated by `ApiKeyAuthFilter`, key stored in `.env`). Swagger/`/actuator/health`/`info`
      remain public on purpose (protecting them is the deploy's job, item 3.8); everything
      else requires the key. No session/login — every request authenticates itself.
- [x] **3.3 Pagination/rate limiting in the Jira adapter** — `nextPageToken` loop (a
      safety cap at 20 pages); 429 handled with retry + backoff honoring `Retry-After`,
      returns what it already collected instead of dropping the whole listing.
- [x] **3.4 Core unit tests** — port fakes (no database/Jira/network):
      AnalyzeCalledService (every branch of the cascade), GapReportService, FeedbackService,
      StandardService, CalledService, mappers (incl. ADF extraction), text
      normalization/matching.
- [x] **3.5 Naming consistency** — `routineCalled` → `routineNumber` in
      `CalledResponse`.
- [x] **3.6 Builder for `Called`/`Standard`** — constructors with 10+ positional
      parameters are a source of silent bugs (swapping arguments of the same type).
      Setters removed at the same time (nothing outside the class used them).
- [x] **3.7 Real `IncidentType`/`FilterCategory`** — `IncidentType` now derives from Jira's
      `issuetype`. **`FilterCategory` defaults to SUPPORT** (every ticket that reaches this
      system today is support) — we haven't found a
      reliable signal in the available fields to tell INFRASTRUCTURE/DEVELOPMENT apart;
      it needs a business decision about which Jira field to use (see `LIMITATIONS.md`).
- [ ] **3.8 Deploy** — package it (Dockerfile), per-environment variables, decide where
      to host it; restrict Swagger/actuator in production.

---

*Convention: one item = one branch = one PR (squash). Items from different phases can
interleave — e.g. 3.1 is small and worth doing before 2.2.*
