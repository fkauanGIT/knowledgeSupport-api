# Changelog

## [1.1.0](https://github.com/fkauanGIT/knowledgeSupport-api/compare/v1.0.0...v1.1.0) (2026-07-17)


### Features

* configure Jira token at runtime via /api/settings/jira ([3c35a51](https://github.com/fkauanGIT/knowledgeSupport-api/commit/3c35a51161a45a813aed096e28843499f14d9dac))

## [1.0.0](https://github.com/fkauanGIT/knowledgeSupport-api/compare/v0.7.0...v1.0.0) (2026-07-14)


### Features

* explicit confidence level on ticket analysis ([e689988](https://github.com/fkauanGIT/knowledgeSupport-api/commit/e6899883d0134dd53f3cf1a36770e19815a4ef5c))
* investigation trail on Standard (hypothesis/query/verification) ([220e8ac](https://github.com/fkauanGIT/knowledgeSupport-api/commit/220e8acfa9d5848e8c181574c14cdde38a063c9c))


### Bug Fixes

* default FilterCategory to SUPPORT instead of PENDING ([3d5b9aa](https://github.com/fkauanGIT/knowledgeSupport-api/commit/3d5b9aabd9fc34a2c6e4e706405132a1aaa1a278))


### Miscellaneous Chores

* mark project as 1.0.0 ([#14](https://github.com/fkauanGIT/knowledgeSupport-api/issues/14)) ([05aa1f4](https://github.com/fkauanGIT/knowledgeSupport-api/commit/05aa1f4f517d10bcfd27cbef9809595055850edf))

## [0.7.0](https://github.com/fkauanGIT/knowledgeSupport-api/compare/v0.6.0...v0.7.0) (2026-07-14)


### ⚠ BREAKING CHANGES

* GapReportResponse and RoutineGapResponse JSON field names changed from Portuguese to English (totalChamadosAnalisados -> totalCalledsAnalyzed, totalSemMatch -> totalWithoutMatch, lacunasPorRotina -> gapsByRoutine, quantidade -> count, percentualDasLacunas -> percentageOfGaps, exemplos -> examples). Clients of GET /api/calleds/gap-report must update their field references.

### Features

* translate public API fields and Swagger docs to English ([dded7cc](https://github.com/fkauanGIT/knowledgeSupport-api/commit/dded7cc5ace3a2d6c62b44f1f51a043b9086da7f))

## [0.6.0](https://github.com/fkauanGIT/knowledgeSupport-api/compare/v0.5.0...v0.6.0) (2026-07-14)


### Features

* autenticacao por API key (X-API-KEY) ([12b2b8f](https://github.com/fkauanGIT/knowledgeSupport-api/commit/12b2b8f0c7f8d50fb621fb978598a35f99878129))
* autenticacao por API key (X-API-KEY) ([d2229a9](https://github.com/fkauanGIT/knowledgeSupport-api/commit/d2229a9eda2748d92f635d82bd2e7fe5c723ba5b))

## [0.5.0](https://github.com/fkauanGIT/knowledgeSupport-api/compare/v0.4.0...v0.5.0) (2026-07-13)


### Features

* jiraKey/status, relatorio de lacunas, feedback e robustez de API (fases 2 e 3) ([1df7ce2](https://github.com/fkauanGIT/knowledgeSupport-api/commit/1df7ce26f197d153d4ee6bc36241404d7059e717))
* matching por containment score com tolerancia a typo (fases 1.1-1.3) ([b6d84e1](https://github.com/fkauanGIT/knowledgeSupport-api/commit/b6d84e1b061a3386ba4132f81884d660f04c81e3))
* matching por score + lacunas/feedback/robustez (fases 1-3 ([cc8b29b](https://github.com/fkauanGIT/knowledgeSupport-api/commit/cc8b29bdb983d42de7ab60163dd4fd43a79e3280))

## [0.4.0](https://github.com/fkauanGIT/knowledgeSupport-api/compare/v0.3.0...v0.4.0) (2026-07-13)


### Features

* adiciona analise automatica de chamados (rotina + nome do erro) ([2105df2](https://github.com/fkauanGIT/knowledgeSupport-api/commit/2105df2569fc1f409d42d6e472c436099474f746))

## [0.3.0](https://github.com/fkauanGIT/knowledgeSupport-api/compare/v0.2.0...v0.3.0) (2026-07-12)


### Features

* adiciona numero de rotina e nome do erro estruturados (Standard e Called) ([b429383](https://github.com/fkauanGIT/knowledgeSupport-api/commit/b4293833f1ade4a8c8ea4ba084c62ea6935a324e))
* adiciona routineNumber ao standard ([0787f14](https://github.com/fkauanGIT/knowledgeSupport-api/commit/0787f146d31dcf41bebe596f81f112dafd27b6b9))
* adicionei um novo campo para o número da rotina, para futuramente conseguir fazer os casos de uso ([8a2b16b](https://github.com/fkauanGIT/knowledgeSupport-api/commit/8a2b16b1a9477413ca65f1cd6c2f5a113e99f6c6))

## [0.2.0](https://github.com/fkauanGIT/knowledgeSupport-api/compare/v0.1.0...v0.2.0) (2026-07-10)


### Features

* adiciona documentacao interativa da API com springdoc/swagger ([23ad22e](https://github.com/fkauanGIT/knowledgeSupport-api/commit/23ad22ebd98808fa0cccb48d4b5c276bd4a74063))

## [0.1.0](https://github.com/fkauanGIT/knowledgeSupport-api/compare/v0.0.1...v0.1.0) (2026-07-09)


### Features

* **domain:** modela entidades iniciais de Called e Requester ([a8a3fbd](https://github.com/fkauanGIT/knowledgeSupport-api/commit/a8a3fbdaed613513b3e92def3ce5da5df33420d3))
* integra chamados do Jira via port/adapter (fatia Called somente leitura) ([b6815ab](https://github.com/fkauanGIT/knowledgeSupport-api/commit/b6815abc462ba84d5ff84e2c7910409d438e94a3))

## Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
