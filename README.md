<div align="center">
<img src="docs/assets/logo.svg" width="160" height="160" alt="Logo knowledgeSupport"/>

# knowledgeSupport API

### Base de conhecimento inteligente para suporte técnico

**Chamados do Jira + padrões de solução = respostas cada vez mais automáticas**

[![Release](https://img.shields.io/github/v/release/fkauanGIT/knowledgeSupport-api?label=release&color=4F46E5)](https://github.com/fkauanGIT/knowledgeSupport-api/releases)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Arquitetura](https://img.shields.io/badge/Arquitetura-Hexagonal-10B981)](docs/ARCHITECTURE.md)

</div>

---

O **knowledgeSupport** integra os chamados do **Jira**, mantém um catálogo de **padrões de erro e soluções** (Standards) e — no roadmap — sugere automaticamente a solução para cada chamado e responde via **Chatwoot**. A cada padrão cadastrado, o sistema "aprende": o mesmo erro nunca precisa ser resolvido duas vezes.

Construído em **Java 17 + Spring Boot** com **Arquitetura Hexagonal** (Ports & Adapters).

## 📚 Documentação

| Documento | O que tem |
|---|---|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | A arquitetura hexagonal do projeto: camadas, ports, adapters, diagramas, fluxos e decisões |
| [docs/FOLDER_STRUCTURE.md](docs/FOLDER_STRUCTURE.md) | Mapa comentado das pastas + "onde colocar o quê" + convenções de nome |
| [docs/CONTRIBUTING.md](CONTRIBUTING.md) | Convenção de commits e versionamento automático (Release Please) |
| [CHANGELOG.md](CHANGELOG.md) | Histórico de versões (gerado automaticamente) |

## 🚀 Como rodar

**Requisitos:** Java 17, Docker (para o PostgreSQL local).

```bash
# 1. Configuração: copie o modelo e preencha (token do Jira etc.)
cp .env.example .env

# 2. Suba o banco local
docker compose up -d

# 3. Rode a aplicação (o Spring lê o .env sozinho)
./mvnw spring-boot:run
```

> O `.env` nunca é commitado (está no `.gitignore`). Para usar um Postgres externo
> (ex: Supabase), basta trocar as variáveis `DB_*` no `.env` — nenhum código muda.

## 🔌 API

A documentação da API é **gerada automaticamente** (springdoc/OpenAPI) e servida pela própria aplicação — sempre atualizada, com exemplos e execução interativa ("Try it out"):

| Endereço (com o app rodando) | O que é |
|---|---|
| [`/swagger-ui.html`](http://localhost:8080/swagger-ui.html) | 📖 Documentação interativa da API (Swagger UI) |
| [`/v3/api-docs`](http://localhost:8080/v3/api-docs) | Especificação OpenAPI 3 em JSON (importável no Postman/Insomnia) |
| [`/actuator/info`](http://localhost:8080/actuator/info) | Versão e commit do build |
| [`/actuator/health`](http://localhost:8080/actuator/health) | Saúde da aplicação |

Em resumo: `/api/standards` (CRUD da base de conhecimento) e `/api/calleds` (chamados ao vivo do Jira, somente leitura). Detalhes de cada rota, campos e exemplos: no Swagger.

## 🧭 A arquitetura em 30 segundos

```
Cliente HTTP ─▶ adapter/in/web ─▶ port/in ─▶ application/service ─▶ port/out ─▶ adapter/out ─▶ Postgres / Jira
                (Controllers)   (UseCases)   (regras de negócio)   (contratos)  (JPA / RestClient)
```

- O **núcleo** (`domain` + `application`) não conhece HTTP, SQL nem Jira.
- **Ports** são interfaces: o núcleo declara o que oferece (`port/in`) e o que precisa (`port/out`).
- **Adapters** traduzem cada fronteira e são trocáveis sem tocar o núcleo.

Detalhes, diagramas e o porquê de cada decisão: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## 🤝 Contribuindo

1. Leia [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) e [docs/FOLDER_STRUCTURE.md](docs/FOLDER_STRUCTURE.md).
2. Siga Conventional Commits (`feat:`, `fix:`, `chore:` — ver [CONTRIBUTING.md](CONTRIBUTING.md)).
3. A versão é automática: `feat`/`fix` na `main` alimentam o Release PR do bot; o merge dele publica a release.

## 🗺️ Roadmap

- [x] CRUD de Standards (PostgreSQL)
- [x] Integração de leitura com o Jira (`Called`)
- [x] Versionamento automático (Release Please + Actuator)
- [x] Rotina e nome do erro estruturados (campos customizados do Jira) nos chamados e padrões
- [ ] `AnalyzeCalledUseCase` — sugerir solução cruzando chamado × padrões
- [ ] Integração Chatwoot (webhook de entrada + resposta automática)
- [ ] Interface web de gestão
