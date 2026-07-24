package com.knowledgeSupport.api.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Atualização das credenciais/config usadas para consultar o Jira. "
        + "Campos em branco preservam o valor atual — em especial o token pode ser omitido "
        + "para alterar só a URL ou o JQL sem reenviar o segredo.")
public record JiraSettingsRequest(

        @Schema(description = "URL base da instância Atlassian", example = "https://sua-empresa.atlassian.net")
        @Size(max = 2048, message = "baseUrl excede 2048 caracteres")
        String baseUrl,

        @Schema(description = "E-mail da conta dona do token", example = "voce@empresa.com")
        String email,

        @Schema(description = "Token da API do Atlassian. Deixe em branco para manter o atual.",
                example = "ATATT3xFfGF0...")
        String apiToken,

        @Schema(description = "JQL usada para listar os chamados", example = "created >= -30d ORDER BY created DESC")
        String jql) {
}
