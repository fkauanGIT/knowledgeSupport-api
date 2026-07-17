package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.adapter.out.jira.JiraCredentials;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Config atual do Jira. O token NUNCA é retornado — apenas se ele está definido.")
public record JiraSettingsResponse(

        @Schema(description = "URL base da instância Atlassian", example = "https://sua-empresa.atlassian.net")
        String baseUrl,

        @Schema(description = "E-mail da conta dona do token", example = "voce@empresa.com")
        String email,

        @Schema(description = "JQL usada para listar os chamados", example = "created >= -30d ORDER BY created DESC")
        String jql,

        @Schema(description = "true se há um token configurado (o valor em si não é exposto)", example = "true")
        boolean tokenConfigured) {

    public static JiraSettingsResponse from(JiraCredentials credentials) {
        return new JiraSettingsResponse(
                credentials.baseUrl(),
                credentials.email(),
                credentials.jql(),
                credentials.apiToken() != null && !credentials.apiToken().isBlank());
    }
}
