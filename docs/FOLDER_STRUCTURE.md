# Estrutura de pastas

> Mapa comentado do repositório. Para o *porquê* de cada camada, leia [ARCHITECTURE.md](ARCHITECTURE.md).

## Visão geral do repositório

```
knowledgeSupport-api/
├── .github/workflows/          # CI: release-please.yml (versionamento automático)
├── docs/                       # 📚 Você está aqui
│   ├── ARCHITECTURE.md
│   ├── CONTRIBUTING.md
│   └── FOLDER_STRUCTURE.md
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
│       ├── Standard.java                 # padrão de erro conhecido + solução
│       ├── Called.java                   # chamado de suporte (vem do Jira)
│       ├── Requester.java                # solicitante (vive dentro do Called)
│       ├── CalledAnalysis.java           # resultado da análise: Called + Standard achado (ou null) + método
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
│   │   │   └── AnalyzeCalledUseCase.java
│   │   └── out/                          # o que o sistema PRECISA de fora (interfaces)
│   │       ├── StandardRepositoryPort.java   # "alguém que persista Standards"
│   │       └── CalledProviderPort.java       # "alguém que forneça (ou busque 1) Called"
│   └── service/                          # implementação dos use cases
│       ├── StandardService.java          # CRUD de padrões (usa StandardRepositoryPort)
│       ├── CalledService.java            # chamados (usa CalledProviderPort)
│       └── AnalyzeCalledService.java     # cruza Called × Standard (usa as duas ports de saída)
│
└── adapter/                              # 🔌 FRONTEIRAS — tradutores
    ├── in/                               # quem RECEBE chamadas do mundo
    │   └── web/                          # canal REST
    │       ├── StandardController.java   # /api/standards (CRUD completo)
    │       ├── StandardRequest.java      # formato do JSON que entra
    │       ├── StandardResponse.java     # formato do JSON que sai
    │       ├── CalledController.java     # /api/calleds (somente GET)
    │       ├── CalledResponse.java
    │       └── CalledAnalysisResponse.java   # resposta de GET /api/calleds/{key}/analysis
    └── out/                              # quem o sistema CHAMA
        ├── persistence/                  # canal PostgreSQL (via JPA)
        │   ├── StandardPersistenceAdapter.java  # implements StandardRepositoryPort
        │   ├── StandardJpaRepository.java       # interface Spring Data
        │   ├── StandardJpaEntity.java           # formato da TABELA (@Entity)
        │   └── StandardMapper.java              # Standard ⇄ StandardJpaEntity
        └── jira/                         # canal API do Jira
            ├── JiraCalledAdapter.java    # implements CalledProviderPort (RestClient)
            ├── JiraCalledMapper.java     # JSON do Jira → Called (extrai texto do ADF)
            ├── JiraSearchResponse.java   # ┐
            ├── JiraIssuePayload.java     # │ records espelhando SÓ os campos
            ├── JiraFields.java           # │ que usamos do JSON do Jira
            ├── JiraStatus.java           # │ (@JsonIgnoreProperties ignora o resto)
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
