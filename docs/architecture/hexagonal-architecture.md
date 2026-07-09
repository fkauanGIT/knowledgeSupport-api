---
name: hexagonal-architecture
description: Como este projeto (knowledgeSupport-api) está estruturado usando arquitetura hexagonal (ports & adapters), usando Java 17 LTS + Spring Boot.
java_version: '17'
status: fatia-de-referencia-implementada (Standard)
date_added: '2026-07-09'
---

## Quando usar esse guia

- Ao adicionar uma nova entidade, caso de uso ou endpoint neste projeto.
- Ao decidir onde uma classe nova deve ficar (`domain`, `application` ou `adapter`).
- Ao revisar um PR e checar se ele respeita os limites da arquitetura.
- Ao entrar como contribuidor e precisar de um modelo mental antes de mexer no código.

## Quando não usar esse guia

- Se você está trabalhando em outro projeto que não segue essa organização.
- Se você procura conselho genérico de Java/Spring Boot sem relação com *como
  esse* código específico está organizado (pra isso, qualquer referência
  padrão de Java 17 / Spring Boot 3.x serve normalmente).

## Objetivo

O `knowledgeSupport-api` é um projeto open source pensado pra crescer com
contribuições externas (novas integrações, novas entidades, novos backends de
armazenamento). Pra isso ser sustentável, o código segue **arquitetura
hexagonal** (também chamada de Ports & Adapters): as regras de negócio moram
num núcleo sem nenhuma dependência de Spring, JPA ou HTTP. Tudo que é
framework é um "adapter" plugável, encaixado no núcleo através de interfaces
explícitas ("portas").

**Importante:** este projeto usa **Java 17 LTS**, não 21+. Ou seja:
- ✅ Disponível e já usado: records (DTOs), classes seladas (`sealed`, se
  precisar depois), pattern matching para `instanceof`, text blocks, `var`.
- ❌ Não disponível: virtual threads / Project Loom (finalizado só no 21),
  pattern matching para `switch` (finalizado no 21), scoped values,
  structured concurrency. Não sugira essas features pra este código.

## A única regra que importa

**As dependências só apontam pra dentro.** `adapter` depende de
`application`, que depende de `domain`. Nunca o contrário.

```
adapter (Spring, JPA, HTTP)  ──depende de──▶  application (casos de uso, portas)  ──depende de──▶  domain (Java puro)
```

Se uma classe em `domain` ou `application` importar qualquer coisa de
`org.springframework.*` ou `jakarta.persistence.*`, isso é uma violação —
pare e mova a anotação/lógica pro adapter correto.

## Estrutura de pacotes (estado atual)

```
com.knowledgeSupport.api/
├── domain/
│   └── model/
│       ├── Standard.java     # POJO puro — sem JPA, sem Spring
│       ├── Called.java       # POJO puro — movido pra cá, ainda sem caso de uso
│       ├── Requester.java     # POJO puro — movido pra cá, ainda sem caso de uso
│       └── enums/
│           ├── IncidentType.java
│           ├── FilterCategory.java
│           └── CategoryStandard.java   # atualmente não usado por nenhuma entidade
│
├── application/
│   ├── port/
│   │   ├── in/                          # o que o sistema CONSEGUE FAZER (casos de uso)
│   │   │   ├── CreateStandardUseCase.java
│   │   │   ├── UpdateStandardUseCase.java
│   │   │   ├── GetStandardUseCase.java
│   │   │   ├── ListStandardsUseCase.java
│   │   │   └── DeleteStandardUseCase.java
│   │   └── out/                         # o que o sistema PRECISA de fora
│   │       └── StandardRepositoryPort.java
│   └── service/
│       └── StandardService.java         # implementa as 5 interfaces de port.in
│
└── adapter/
    ├── in/
    │   └── web/
    │       ├── StandardController.java  # REST, depende só de port.in
    │       ├── StandardRequest.java     # DTO de entrada (record)
    │       └── StandardResponse.java    # DTO de saída (record)
    └── out/
        └── persistence/
            ├── StandardJpaEntity.java          # @Entity mora AQUI, não no domain
            ├── StandardJpaRepository.java       # interface do Spring Data
            ├── StandardMapper.java              # domínio <-> entidade JPA
            └── StandardPersistenceAdapter.java  # implementa StandardRepositoryPort
```

`Standard` é a **fatia vertical de referência** — totalmente ligada de ponta a
ponta. `Called` e `Requester` foram movidos pro `domain/model` puro e tiveram
as anotações JPA removidas, mas ainda não têm caso de uso/adapter porque
nenhum comportamento foi pedido pra eles. São os próximos PRs naturais,
seguindo exatamente o mesmo padrão do `Standard`.

## Como cada camada funciona

### `domain/model`
Classes Java puras. Nenhuma anotação de framework. Construtor + getters/setters
+ `toString()`. É o que você escreveria num projeto sem Spring nenhum. Testável
sem nenhuma configuração — só `new Standard(...)`.

### `application/port/in`
Uma interface por caso de uso, um método cada. Essa granularidade é
intencional: um controller (ou qualquer outro ponto de entrada — uma CLI, um
job agendado, um listener de mensagem) depende só da capacidade exata que
precisa, e fica trivial mockar em teste.

```java
public interface CreateStandardUseCase {
    Standard create(Standard standard);
}
```

### `application/port/out`
Uma interface descrevendo o que o núcleo precisa do mundo externo (nesse caso,
persistência). O núcleo não sabe nem se importa se a implementação é Postgres,
um mapa em memória, ou uma chamada REST pra outro serviço.

### `application/service`
Implementa as interfaces de `port.in`, depende só das interfaces de `port.out`.
`StandardService` tem a anotação `@Service` (essa é a única dependência de
Spring permitida aqui — a anotação em si, não a maquinaria de dados/web do
framework).

### `adapter/out/persistence`
Onde o JPA realmente mora: `@Entity`, `@Id`, `@GeneratedValue`,
`@Enumerated(EnumType.STRING)`. Um `Mapper` traduz de ida e volta entre a
entidade JPA e o objeto de domínio. O `PersistenceAdapter` implementa a
interface de `port.out` usando Spring Data JPA.

**Por que a separação, concretamente:** `StandardJpaEntity` usa
`@Enumerated(EnumType.STRING)` no campo `incidentType` — uma decisão
específica do JPA pra evitar o bug clássico de mapeamento ordinal (reordenar
os valores do enum corrompe silenciosamente os dados já salvos). Essa decisão
pertence ao adapter de persistência, não ao modelo de domínio, que não tem
opinião sobre como o enum é armazenado.

### `adapter/in/web`
O `@RestController` depende das interfaces de `port.in` (nunca da classe de
serviço direto, nunca do repository). Objetos de domínio nunca atravessam a
fronteira HTTP diretamente — `StandardRequest`/`StandardResponse` são os DTOs
que fazem isso.

## Como adicionar uma nova fatia vertical (ex: ligar o `Called`)

1. `Called` já existe como modelo de domínio puro — nenhuma mudança necessária aí.
2. Criar `application/port/in/{Create,Update,Get,List,Delete}CalledUseCase.java`.
3. Criar `application/port/out/CalledRepositoryPort.java`.
4. Criar `application/service/CalledService.java` implementando as interfaces
   de port.in, dependendo só de `CalledRepositoryPort`.
5. Criar `adapter/out/persistence/CalledJpaEntity.java` +
   `CalledJpaRepository.java` + `CalledMapper.java` +
   `CalledPersistenceAdapter.java`.
6. Criar `adapter/in/web/CalledController.java` + `CalledRequest`/`CalledResponse`.
7. O Spring liga tudo automaticamente via component scan — não precisa de
   `@Configuration` manual desde que exatamente uma classe implemente cada
   interface de porta.

## Estratégia de teste por camada

- **`domain`**: JUnit 5 puro, sem contexto Spring, sem banco. Os testes mais
  rápidos da suíte — deveriam ser a maioria da cobertura.
- **`application/service`**: mockar a interface de `port.out` com Mockito,
  testar a lógica do caso de uso isolada.
- **`adapter/out/persistence`**: `@DataJpaTest` ou Testcontainers contra um
  Postgres real — verificar o mapper e o comportamento real do
  SQL/Hibernate.
- **`adapter/in/web`**: `@WebMvcTest` mockando as interfaces de `port.in` —
  verificar status HTTP, formato do JSON e roteamento, não a lógica de
  negócio.

## Convenções (cobrar isso em code review)

- Nenhum import de `jakarta.persistence.*` ou `org.springframework.*` dentro
  de `domain/model`. Nunca.
- Nenhum objeto de domínio serializado direto como resposta HTTP — sempre
  passar por um DTO em `adapter/in/web`.
- Nenhuma entidade JPA vazando pra `application` ou `domain` — sempre passar
  pelo `Mapper`.
- Uma interface por caso de uso em `port.in`, não uma interface gorda de
  "CRUD service" — mantém o mock e a responsabilidade única limpos conforme o
  sistema cresce.
- Campos enum devem usar `@Enumerated(EnumType.STRING)` na entidade JPA —
  nunca confiar no mapeamento ordinal padrão.

## Exemplos de pedidos (contribuidor / assistente de IA)

- "Adicionar um filtro por `CategoryStandard` no `ListStandardsUseCase`" →
  mexe em `port.in`, `StandardService`, `StandardRepositoryPort` e na query
  JPA — o domínio não muda a menos que o filtro precise de novo estado no
  domínio.
- "Ligar o `Called` seguindo o mesmo padrão do `Standard`" → seguir os sete
  passos acima.
- "Adicionar anexos de PDF ao `Standard`" → novo `FileStoragePort` em
  `application/port/out`, um `LocalFileStorageAdapter` (ou adapter de S3) em
  `adapter/out/storage`, sem mudar `StandardRepositoryPort`.
- "Trocar Postgres por outro banco" → só mexe em `adapter/out/persistence`.
  Zero mudança em `domain` ou `application`.

## Limitações

- Este documento descreve a arquitetura como implementada na fatia
  `Standard`. `Called` e `Requester` são só domínio até seus casos de uso
  serem definidos — não assuma que já têm portas/adapters.
- O enum `CategoryStandard` existe em `domain/model/enums` mas não está
  ligado a nenhuma entidade — decisão de negócio ainda pendente, não um
  esquecimento pra corrigir por conta própria.
- Suporte a PDF/anexo foi propositalmente deixado de fora — ainda não
  implementado.
- Alvo é Java 17 LTS. Não introduza features exclusivas do Java 21+ (virtual
  threads, pattern matching em `switch`, structured concurrency) sem antes
  confirmar que a versão do Java do projeto foi atualizada.
