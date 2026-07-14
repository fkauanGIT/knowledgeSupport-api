# Backlog — knowledgeSupport-api

> Documento duplo: (1) registro do que falta fazer, para não se perder; (2) **prompt pronto
> para colar em uma IA** ao trabalhar em qualquer item. Copie daqui de cima até o fim do
> bloco "Contexto para IA" + o item que for implementar.

---

## Contexto para IA (cole isto no início de qualquer sessão)

Você vai trabalhar no **knowledgeSupport-api**: API Java 17 + Spring Boot (4.x, Maven) em
**Arquitetura Hexagonal**. O sistema integra chamados do Jira (projeto SUP, request type
"Erros/Alertas no sistema WINTHOR"), mantém um catálogo de padrões de erro com soluções
(`Standard`, PostgreSQL) e analisa chamados sugerindo a solução cadastrada
(`GET /api/calleds/{key}/analysis`).

**Regras inegociáveis do projeto:**

1. **Regra de dependência**: `adapter → application → domain`. `domain` e `application`
   não importam NADA de web/JPA/Jackson/HTTP (exceção: anotações `@Service` do Spring).
   Formatos externos (JSON do Jira, entidades JPA) morrem nos mappers dos adapters.
2. **Ports**: capacidade nova que o núcleo precisa = interface em `application/port/out` +
   adapter. Serviço novo oferecido = interface em `application/port/in` + service.
   Fornecedor novo de capacidade existente = só adapter novo.
3. **Sem dependência nova sem justificar** — e nunca outra linguagem no repositório.
4. **Segredos só no `.env`** (gitignored). Nunca em código, yaml commitado ou docs.
5. **Conventional Commits** (`feat:`/`fix:`/`docs:`/`chore:`) — versão é automática
   (Release Please). Trabalhar em branch, PR para main com squash merge.
6. Documentar decisões novas em `docs/ARCHITECTURE.md` (tabela de decisões) e manter
   `docs/LIMITATIONS.md` honesto.
7. Endpoints documentados com anotações springdoc **apenas em `adapter/in/web`**.
8. Leia antes de codar: `docs/ARCHITECTURE.md`, `docs/FOLDER_STRUCTURE.md`,
   `docs/LIMITATIONS.md`.

**Estado atual:** CRUD de Standards com `routineNumber` e taxa de acerto por feedback ·
chamados do Jira com rotina, nome do erro, jiraKey e status (paginado, retry em 429) ·
análise em cascata (match exato → containment score+Levenshtein, `routineNumber` como
filtro) · relatório de lacunas por rotina · 404 tratado centralmente · Swagger ·
autenticação por API key · versionamento automático.

---

## FASE 1 — Matching de verdade (resolve o LIMITATIONS.md)

- [x] **1.1 Normalizar a comparação atual** — minúsculas, sem acentos
      (`java.text.Normalizer`), trim e colapso de espaços antes de comparar `errorName`
      × `standardName`. Quick win; elimina falhas bobas de digitação de caixa/acento.
- [x] **1.2 Matching textual com score** — tokenizar `titleCalled + descriptionCalled +
      errorName` do chamado e `standardName + text` do Standard; remover stopwords PT;
      similaridade Jaccard; `routineNumber` vira **filtro** (reduz candidatos), não par
      obrigatório. Resultado com score 0–1 e limiar configurável no yaml. `MatchMethod`
      ganha score/confidence no response.
      **Nota de implementação:** Jaccard simétrico (interseção/união) penaliza Standard
      rico — quanto mais um Standard acumula variações de sintoma, pior o score fica, o
      oposto do desejado. Trocado por *containment* (interseção/tokens do chamado),
      assimétrico de propósito. Ver `docs/ARCHITECTURE.md` (tabela de decisões).
- [x] **1.3 Tolerância a typo** — Levenshtein via Apache Commons Text na comparação de
      tokens (única dependência nova permitida nesta fase).
- [ ] **1.4 (quando a base crescer) Full-text search do PostgreSQL** — tsvector/tsquery
      via port (`StandardSearchPort` ou método na RepositoryPort) para não varrer tudo
      em memória.
- [ ] **1.5 (futuro, opcional) Camada semântica** — port de matching semântico
      (embeddings/IA) como MAIS UM degrau da cascata, nunca substituindo os
      determinísticos. IA = "respostas bonitas"/último recurso, não dependência.
- [ ] **1.6 Remodelar `Standard` para fluxo N3** — trilha de investigação (passos:
      hipótese, tabela/query, verificação) em vez de `result` único. Mudança de domínio
      grande: fazer DEPOIS que 1.2 provar que encontra o Standard certo.

## FASE 2 — Valor visível (produto)

- [x] **2.1 `jiraKey` (e status) no `Called`** — pré-requisito dos itens abaixo:
      referenciar o chamado na origem. Mapear também o status real (statusCategory).
      **Nota:** mapeado o nome do status (`fields.status().name()`); `statusCategory`
      (new/indeterminate/done) não foi persistido em campo próprio — não havia uso definido
      pra ele ainda, adicionar quando surgir necessidade concreta.
- [ ] **2.2 Comentário automático no Jira** — quando a análise encontra Standard, postar
      comentário no ticket com a solução (port `TicketCommentPort` + escrita no
      JiraAdapter). A feature que faz a equipe VER o sistema trabalhando.
- [x] **2.3 Relatório de lacunas** — `GET /api/calleds/gap-report` agrega análises sem
      match, agrupadas por rotina e frequência: "cadastre estes padrões e cubra X% do
      volume". Dirige a alimentação da base.
- [x] **2.4 Feedback resolveu/não resolveu** — `POST /api/calleds/{key}/feedback` +
      `GET /api/standards/{id}/accuracy` — taxa de acerto por Standard (confiança
      auditável, o que IA não dá).
- [ ] **2.5 Chatwoot** — `adapter/in/chatwoot` (webhook de conversa) e
      `adapter/out/chatwoot` (resposta ao solicitante).
- [ ] **2.6 Interface web de gestão** — front separado consumindo a API (Standards +
      análises + relatórios).

## FASE 3 — Robustez e qualidade

- [x] **3.1 Erros HTTP corretos** — `GlobalExceptionHandler` (`@RestControllerAdvice`):
      chamado/Standard inexistente → 404 com corpo explicativo, centralizado (antes cada
      controller repetia o mesmo catch).
- [x] **3.2 Autenticação** — Spring Security com API key (`X-API-KEY`, header validado por
      `ApiKeyAuthFilter`, chave em `.env`). Swagger/`/actuator/health`/`info` seguem públicos
      de propósito (protegê-los é responsabilidade do deploy, item 3.8); todo o resto exige
      a chave. Sem sessão/login — cada requisição se autentica sozinha.
- [x] **3.3 Paginação/rate limit no adapter Jira** — `nextPageToken` em loop (trava de
      segurança em 20 páginas); 429 com retry + backoff respeitando `Retry-After`,
      devolve o que já coletou em vez de derrubar a listagem inteira.
- [x] **3.4 Testes de unidade do núcleo** — fakes das ports (sem banco/Jira/rede):
      AnalyzeCalledService (todos os ramos da cascata), GapReportService, FeedbackService,
      StandardService, CalledService, mappers (incl. extração ADF), normalização/matching
      textual.
- [x] **3.5 Consistência de nomes** — `routineCalled` → `routineNumber` no
      `CalledResponse`.
- [x] **3.6 Builder para `Called`/`Standard`** — construtores com 10+ parâmetros
      posicionais são fonte de bug silencioso (troca de argumentos da mesma natureza).
      Setters removidos junto (nada fora da classe os usava).
- [x] **3.7 `IncidentType`/`FilterCategory` reais** — `IncidentType` deriva do `issuetype`
      do Jira agora. **`FilterCategory` continua PENDING fixo** — não achamos sinal
      confiável nos campos disponíveis pra derivar SUPPORT/INFRASTRUCTURE/DEVELOPMENT;
      precisa de decisão de negócio sobre qual campo do Jira usar (ver `LIMITATIONS.md`).
- [ ] **3.8 Deploy** — empacotar (Dockerfile), variáveis por ambiente, decidir onde
      hospedar; Swagger/actuator restritos em produção.

---

*Convenção: um item = uma branch = um PR (squash). Itens de fase diferentes podem
intercalar — ex: 3.1 é pequeno e vale antes de 2.2.*
