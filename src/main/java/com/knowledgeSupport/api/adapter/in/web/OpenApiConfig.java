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
 * OpenAPI/Swagger configuration — lives in the web adapter because documenting
 * the REST API is the HTTP boundary's business, not the core's.
 * The displayed version comes from the build (Maven build-info), synced with
 * Release Please; running from the IDE without a Maven build falls back to "dev".
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
                                Technical support knowledge base.

                                - **Calleds**: pulled live from Jira (project SUP) — read-only.
                                - **Standards**: catalog of known errors and their solutions, persisted in PostgreSQL.
                                - **Roadmap**: automatic analysis (ticket × pattern) and Chatwoot integration.

                                Hexagonal Architecture (Ports & Adapters) — see the architecture docs in the repository.

                                **Authentication**: every endpoint requires the `X-API-KEY` header. Click "Authorize" and paste the key.""")
                        .version(version)
                        .contact(new Contact().name("Francisco Kauan").email("kauan.ti@grupocoagro.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Architecture documentation (docs/ARCHITECTURE.md)")
                        .url("https://github.com/fkauanGIT/knowledgeSupport-api/blob/main/docs/ARCHITECTURE.md"))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME))
                .schemaRequirement(API_KEY_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-KEY"));
    }
}
