package com.knowledgeSupport.api.adapter.in.web;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Configuração do OpenAPI/Swagger — vive no adapter web porque documentar
 * a API REST é assunto da fronteira HTTP, não do núcleo.
 * A versão exibida vem do build (Maven build-info), sincronizada com o
 * Release Please; rodando pela IDE sem build Maven, cai no fallback "dev".
 */
@Configuration
public class OpenApiConfig {

    private static final String API_KEY_SCHEME = "ApiKeyAuth";

    @Bean
    public OpenAPI knowledgeSupportOpenApi(Optional<BuildProperties> buildProperties) {
        String version = buildProperties.map(BuildProperties::getVersion).orElse("dev");
        return new OpenAPI()
                .info(new Info()
                        .title("knowledgeSupport API")
                        .description("""
                                Base de conhecimento de suporte técnico.

                                - **Chamados (Calleds)**: puxados ao vivo do Jira (projeto SUP) — somente leitura.
                                - **Padrões (Standards)**: catálogo de erros conhecidos e suas soluções, persistido em PostgreSQL.
                                - **Roadmap**: análise automática (chamado × padrão) e integração com Chatwoot.

                                Arquitetura Hexagonal (Ports & Adapters) — ver documentação de arquitetura no repositório.

                                **Autenticação**: todos os endpoints exigem o header `X-API-KEY`. Clique em "Authorize" e cole a chave.""")
                        .version(version)
                        .contact(new Contact().name("Francisco Kauan").email("kauan.ti@grupocoagro.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentação de arquitetura (docs/ARCHITECTURE.md)")
                        .url("https://github.com/fkauanGIT/knowledgeSupport-api/blob/main/docs/ARCHITECTURE.md"))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME))
                .schemaRequirement(API_KEY_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-KEY"));
    }
}
