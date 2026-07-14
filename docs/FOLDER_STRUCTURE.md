# Estrutura de pastas

> Mapa comentado do repositório. Para o *porquê* de cada camada, leia [ARCHITECTURE.md](ARCHITECTURE.md).

## Visão geral do repositório

```
knowledgeSupport-api/
├── .github/workflows/          # CI: release-please.yml (versionamento automático)
├── docs/                       # 📚 Você está aqui
│   ├── ARCHITECTURE.md
│   ├── CONTRIBUTING.md
│   ├── FOLDER_STRUCTURE.md
│   ├── LIMITATIONS.md          # onde a arquitetura bonita NÃO disfarça o problema de domínio
│   └── BACKLOG.md              # o que falta fazer + prompt pronto pra colar numa IA
├── src/main/java/com/knowledgeSupport/api/   # código-fonte (detalhado abaixo)
├── src/main/resources/
│   └── application.yaml        # config do Spring (lê variáveis do .env)
├── src/test/java/...           # testes
├── .env.example                # modelo de configuração (copie para .env)
├── .env                        # ⚠️ seus segredos — NUNCA commitado (.gitignore)
├── docker-compose.yml          # PostgreSQL local para desenvolvimento
├── pom.xml                     # dependências Maven + versão do projeto
├── CHANGELOG.md                # gerado pelo Release Please — não edite à mão
├── release-please-config.json  # config do bot de release
└── .release-please-manifest.json
```

## O código-fonte (o hexágono em pastas)

```
com.knowledgeSupport.api
│
├── DemoApplication.java                  # ponto de partida do Spring Boot
│
├── domain/                               # ⬡ CENTRO — vocabulário do negócio
│   └── model/
│       ├── Standard.java                 # padrão de erro conhecido + solução (builder, sem setters)
│       ├── Called.java                   # chamado de suporte (vem do Jira; builder, sem setters)
│       ├── Requester.java                # solicitante (vive dentro do Called)
│       ├── CalledAnalysis.java           # resultado da análise: Called + Standard achado (ou null) + método
│       ├── MatchMethod.java              # como o match foi achado: nome + score (0-1)
│       ├── GapReport.java                # relatório agregado: total analisado, total sem match, lacunas por rotina
│       ├── RoutineGap.java               # quantidade + % das lacunas + exemplos de uma rotina
│       ├── Feedback.java                 # resolveu ou não resolveu, ligado a um Standard por id
│       ├── StandardAccuracy.java         # taxa de acerto agregada de um Standard
│       └── enums/
│           ├── IncidentType.java         # ALERT, ERROR
│           ├── FilterCategory.java       # SUPPORT, INFRASTRUCTURE, DEVELOPMENT, PENDING
│           └── CategoryStandard.java
│
├── application/                          # ⬡ NÚCLEO — regras e contratos
│   ├── port/
│   │   ├── in/                           # o que o sistema OFERECE (interfaces)
│   │   │   ├── CreateStandardUseCase.java
│   │   │   ├── GetStandardUseCase.java
│   │   │   ├── ListStandardsUseCase.java
│   │   │   ├── UpdateStandardUseCase.java
│   │   │   ├── DeleteStandardUseCase.java
│   │   │   ├── ListCalledsUseCase.java
│   │   │   ├── AnalyzeCalledUseCase.java
│   │   │   ├── GapReportUseCase.java
│   │   │   ├── SubmitFeedbackUseCase.java
│   │   │   └── GetStandardAccuracyUseCase.java
│   │   └── out/                          # o que o sistema PRECISA de fora (interfaces)
│   │       ├── StandardRepositoryPort.java   # "alguém que persista Standards"
│   │       ├── CalledProviderPort.java       # "alguém que forneça (ou busque 1) Called"
│   │       └── FeedbackRepositoryPort.java   # "alguém que persista Feedback"
│   └── service/                          # implementação dos use cases
│       ├── StandardService.java          # CRUD de padrões (usa StandardRepositoryPort)
│       ├── CalledService.java            # chamados (usa CalledProviderPort)
│       ├── AnalyzeCalledService.java     # busca Called+Standards e delega pro CalledStandardMatcher
│       ├── CalledStandardMatcher.java    # a cascata de match em si — Java puro, reusado pelo GapReportService
│       ├── GapReportService.java         # roda a cascata em lote sobre todos os chamados abertos
│       ├── FeedbackService.java          # registra feedback + calcula taxa de acerto por Standard
│       └── TextSimilarity.java           # normalize/tokenize/score — Java puro, sem Spring, sem I/O
│
└── adapter/                              # 🔌 FRONTEIRAS — tradutores
    ├── in/                               # quem RECEBE chamadas do mundo
    │   └── web/                          # canal REST
    │       ├── StandardController.java   # /api/standards (CRUD + /accuracy)
    │       ├── StandardRequest.java      # formato do JSON que entra
    │       ├── StandardResponse.java     # formato do JSON que sai
    │       ├── StandardAccuracyResponse.java
    │       ├── CalledController.java     # /api/calleds (GET, /gap-report, /feedback)
    │       ├── CalledResponse.java
    │       ├── CalledAnalysisResponse.java   # resposta de GET /api/calleds/{key}/analysis
    │       ├── GapReportResponse.java
    │       ├── RoutineGapResponse.java
    │       ├── FeedbackRequest.java
    │       ├── FeedbackResponse.java
    │       ├── GlobalExceptionHandler.java   # @RestControllerAdvice: NoSuchElementException -> 404
    │       └── security/
    │           ├── ApiKeyAuthFilter.java     # valida header X-API-KEY contra .env
    │           └── SecurityConfig.java       # @EnableWebSecurity: exige a chave, exceto Swagger/health
    └── out/                              # quem o sistema CHAMA
        ├── persistence/                  # canal PostgreSQL (via JPA)
        │   ├── StandardPersistenceAdapter.java  # implements StandardRepositoryPort
        │   ├── StandardJpaRepository.java       # interface Spring Data
        │   ├── StandardJpaEntity.java           # formato da TABELA (@Entity, text/result sem limite de 255)
        │   ├── StandardMapper.java              # Standard ⇄ StandardJpaEntity
        │   ├── FeedbackPersistenceAdapter.java  # implements FeedbackRepositoryPort
        │   ├── FeedbackJpaRepository.java
        │   ├── FeedbackJpaEntity.java
        │   └── FeedbackMapper.java
        └── jira/                         # canal API do Jira
            ├── JiraCalledAdapter.java    # implements CalledProviderPort (RestClient, paginado, retry em 429)
            ├── JiraCalledMapper.java     # JSON do Jira → Called (extrai texto do ADF, tira timestamp do errorName)
            ├── JiraSearchResponse.java   # ┐
            ├── JiraIssuePayload.java     # │ records espelhando SÓ os campos
            ├── JiraFields.java           # │ que usamos do JSON do Jira
            ├── JiraStatus.java           # │ (@JsonIgnoreProperties ignora o resto)
            ├── JiraIssueType.java        # │
            ├── JiraReporter.java         # │
            └── JiraDoc.java              # ┘ nó do ADF (texto rico, recursivo)
```

## Onde colocar o quê (cola rápida)

| Vou criar... | Vai em... |
|---|---|
| Um conceito novo do negócio (ex: `Solution`) | `domain/model/` |
| Uma operação nova que o sistema oferece | interface em `application/port/in/` + implementação em `application/service/` |
| Uma necessidade nova de algo externo (banco, API, e-mail) | interface em `application/port/out/` + adapter em `adapter/out/<canal>/` |
| Um endpoint REST novo | `adapter/in/web/` (controller + records) |
| Um canal de entrada novo (webhook, scheduler, fila) | `adapter/in/<canal>/` |
| O formato JSON de um sistema externo | dentro do adapter desse sistema (ex: `adapter/out/jira/`) — nunca no domínio |
| Conversão entre formato externo e domínio | um `*Mapper` dentro do adapter da fronteira |
| Configuração/segredo (token, URL) | `.env` (valor real) + `.env.example` (modelo) + `application.yaml` (placeholder) |

## Convenções de nome

- **Ports de entrada**: `<Verbo><Conceito>UseCase` — ex: `CreateStandardUseCase`, `ListCalledsUseCase`.
- **Ports de saída**: `<Conceito><Papel>Port` — ex: `StandardRepositoryPort`, `CalledProviderPort`.
- **Adapters**: `<Conceito><Canal>Adapter` — ex: `StandardPersistenceAdapter`, `JiraCalledAdapter`.
- **Mappers**: `<Origem/Conceito>Mapper` — classe `final` com métodos estáticos `toDomain`/`toEntity`.
- **DTOs da web**: `<Conceito>Request` / `<Conceito>Response` (records).
- **Payloads externos**: prefixo do sistema — `Jira*`, futuramente `Chatwoot*`.

## Teste rápido de sanidade (antes de abrir PR)

1. `domain/` importa algo além de Java puro e outros domínios? ❌
2. `application/` importa algo de `adapter/`, JPA, Jackson ou web? ❌ (única exceção atual: anotação `@Service` do Spring)
3. Algum formato externo (`Jira*`, `*JpaEntity`) aparece fora do seu adapter? ❌
4. Segredo hardcoded em código ou yaml commitado? ❌ (sempre `.env`)
5. Commit segue Conventional Commits (`feat:`, `fix:`, `chore:`...)? ✅ (ver [CONTRIBUTING.md](../CONTRIBUTING.md))
