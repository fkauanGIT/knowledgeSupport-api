# Contribuindo

## Versionamento

A versão deste projeto é **gerada automaticamente** — não edite o `<version>`
do `pom.xml` manualmente nem crie tags.

Basta seguir o padrão de commit abaixo, que o resto acontece sozinho:

- `fix: descrição` → correção de bug (sobe versão patch)
- `feat: descrição` → nova funcionalidade (sobe versão minor)
- `feat!: descrição` (ou rodapé `BREAKING CHANGE:`) → mudança incompatível (sobe versão major)

Ao dar push/merge no `main`, um bot (Release Please) abre um PR próprio
propondo a nova versão. Você não precisa mexer nisso.