# demo

Projeto Spring Boot (Java 17) com JPA e PostgreSQL.

## Requisitos

- Java 17
- Docker (para subir o banco PostgreSQL)

## Como rodar

1. Suba o banco de dados:

   docker compose up -d

2. Rode a aplicação:

   ./mvnw spring-boot:run

## Configuração

Copie o `.env.example` para `.env` e ajuste as variáveis conforme necessário.