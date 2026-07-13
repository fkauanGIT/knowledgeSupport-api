# Limitações do knowledgeSupport-api

> Este documento existe pra não deixar a arquitetura bonita disfarçar um problema de domínio.
> Leia junto com [ARCHITECTURE.md](ARCHITECTURE.md).

## O problema central: o matching ainda não entende sinônimo, só texto parecido

**Atualizado (fase 1.1–1.3 do `BACKLOG.md`):** o matcher deixou de exigir igualdade exata
de string. `AnalyzeCalledService.analyze` agora tenta, em cascata:

1. Match exato: `routineNumber` igual + `errorName`/`standardName` normalizados (sem
   acento/caixa/espaço duplicado, via `TextSimilarity.normalize`) — mesmo comportamento de
   antes, só que tolerante a diferença de digitação trivial.
2. Score de similaridade de texto (`TextSimilarity.score`): tokeniza
   `titleCalled+descriptionCalled+errorName` contra `standardName+text`, remove stopwords PT
   (preservando negação — "não" nunca é descartado) e mede **containment** (quanto do chamado
   está coberto pelo Standard, não o inverso) com tolerância a typo (Levenshtein).
   Assimétrico de propósito: um Standard rico, cobrindo vários jeitos de descrever o mesmo
   bug, não deveria ser penalizado por ter mais texto do que o chamado. `routineNumber` virou
   **filtro que prioriza candidatos**, não mais par obrigatório: um chamado sem rotina
   preenchida, ou com rotina errada, ainda pode encontrar Standard pelo texto.
3. Threshold configurável (`matching.threshold`, default 0.4) decide o que conta como match;
   o score fica visível no `MatchMethod` e na resposta da API (`CalledAnalysisResponse.score`).

Isso já resolve o caso de digitação divergente trivial ("Caixa não fecha" vs "caixa nao
fecha") e erro de digitação (Levenshtein). **Não resolve** o caso mais difícil, que segue
sendo o cenário real de N3:

- O containment score mede **sobreposição de palavras**, não significado. *"Fechei um caixa
  ontem e ele ainda consta em aberto"* e *"caixa preso em aberto após fechamento"*
  compartilham poucas palavras exatas (ou próximas por Levenshtein) apesar de serem o mesmo
  problema — isso só um estágio semântico (embeddings, item 1.5 do backlog, ainda não
  implementado) resolveria. Pior ainda quando a causa raiz nunca foi escrita no chamado
  original (ex: um sintoma de UI que só se conecta a um problema de configuração fiscal
  depois de investigado) — nem embeddings resolveriam isso, porque a palavra-chave da causa
  simplesmente não existe no texto de nenhum dos dois lados até alguém descobrir e cadastrar.
- `Standard.result` continua um campo único de "resposta final", não uma trilha de
  investigação (item 1.6, também não implementado).
- A base de Standards ainda é varrida inteira em memória (`findAll()`); full-text search do
  Postgres (item 1.4) só entra quando o volume justificar.

**Consequência prática:** o sistema hoje pega bem paráfrase leve e erro de digitação, mas
ainda depende de vocabulário compartilhado entre quem abriu o chamado e quem cadastrou o
Standard. Dois relatos com vocabulário totalmente diferente para o mesmo bug real ainda
não se encontram.

Trocar de banco, trocar o Jira, adicionar Chatwoot — nada disso resolve isso. A arquitetura
hexagonal protege o núcleo de mudanças de infraestrutura; ela não corrige uma regra de
negócio malformada. O defeito está dentro do hexágono, não na borda.

## Outras limitações conhecidas (não são o foco deste documento, mas existem)

| Limitação | Onde | Impacto |
|---|---|---|
| Sem autenticação/autorização nos endpoints | `adapter/in/web/*Controller` | Não é deploy-ready fora de rede totalmente confiável |
| `Called` nunca é persistido, sempre busca ao vivo no Jira | `CalledProviderPort` / `JiraCalledAdapter` | Sem paginação ou tratamento de rate limit visível — risco em volume alto |
| `NoSuchElementException` sobe como 500 genérico | `CalledController` | Erro de "chamado não encontrado" não é distinguível de erro real de servidor |
| Cobertura de teste mínima fora do matching | `AnalyzeCalledService`/`TextSimilarity` cobertos; `JiraCalledMapper` só cobre `extractText`/`stripTimestampPrefix`, não `toDomain` inteiro (parsing de data, `routineNumber`) | Regressão em parsing de data/rotina do Jira não é pega por teste |
| Score de texto é sobreposição de palavras, não semântica | `TextSimilarity.score` | Sinônimo/paráfrase distante ("caixa não fecha" x "operador não consegue encerrar o dia") ainda não casa — precisa do item 1.5 (embeddings) |

## O que precisaria mudar pra atender um fluxo de N3

Isto é redesenho de regra de negócio dentro do núcleo, não um adapter novo:

1. ~~Substituir igualdade exata em `errorName` por busca textual sobre `descriptionCalled` +
   `Standard.text` (mantendo `routineNumber` como filtro, não par obrigatório).~~ **Feito** —
   fuzzy match por containment score + Levenshtein, ver seção acima. Falta o degrau semântico
   (embeddings) pra paráfrase distante — e mesmo esse degrau não resolve causa raiz nunca
   escrita no chamado original (ver exemplo acima).
2. Modelar `Standard` como uma sequência de passos de investigação (tabelas, campos,
   queries, hipóteses descartadas), não um único campo `result`. Ainda não feito — depende
   do item 1 estar validado em uso real (item 1.6 do `BACKLOG.md`).
3. Camada semântica (embeddings) como último degrau da cascata, nunca substituindo os
   métodos determinísticos acima (item 1.5 do `BACKLOG.md`, opcional/futuro).

Com o item 1 feito, o sistema já pega paráfrase leve e erro de digitação — mas ainda serve
melhor a erros semi-determinísticos do que a diagnóstico de causa raiz N3 com vocabulário
totalmente livre. Os itens 2 e 3 são o que falta pra isso mudar de verdade.
