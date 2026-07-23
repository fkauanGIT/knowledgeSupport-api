package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.adapter.out.jira.JiraCredentials;
import com.knowledgeSupport.api.adapter.out.jira.JiraCredentialsValidator;
import com.knowledgeSupport.api.adapter.out.jira.JiraSettingsStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound adapter para configurar a integração com o Jira em runtime.
 *
 * <p>Existe para resolver a rotação de token: quando o token do Atlassian expira, o
 * operador atualiza aqui (pela interface) sem editar o {@code .env} nem reiniciar a API.
 * Protegido pelo mesmo {@code X-API-KEY} dos demais endpoints; o token do Jira nunca é
 * devolvido pelo GET, só a informação de que existe um configurado.</p>
 */
@RestController
@RequestMapping("/api/settings/jira")
@Tag(name = "Settings", description = "Configuração em runtime da integração com o Jira (rotação de token sem editar .env).")
public class JiraSettingsController {

    private final JiraSettingsStore settingsStore;
    private final JiraCredentialsValidator validator;

    public JiraSettingsController(JiraSettingsStore settingsStore, JiraCredentialsValidator validator) {
        this.settingsStore = settingsStore;
        this.validator = validator;
    }

    @GetMapping
    @Operation(summary = "Config atual do Jira",
            description = "Retorna URL base, e-mail e JQL, além de indicar se há um token configurado. O token em si nunca é exposto.")
    @ApiResponse(responseCode = "200", description = "Config atual")
    public JiraSettingsResponse get() {
        return JiraSettingsResponse.from(settingsStore.current());
    }

    @PutMapping
    @Operation(summary = "Atualiza a config do Jira",
            description = "Atualiza URL base, e-mail, JQL e/ou token. Campos em branco preservam o valor atual — "
                    + "envie o token só quando quiser trocá-lo. Antes de persistir, faz uma chamada de teste ao "
                    + "Jira com a config resultante; se o Jira rejeitar (JQL inválida, baseUrl/token errado), a "
                    + "requisição falha com 400 e nada é salvo. O override é persistido e sobrevive a reinícios.")
    @ApiResponse(responseCode = "200", description = "Config atualizada")
    @ApiResponse(responseCode = "400", description = "Jira rejeitou a config informada — nada foi salvo")
    public JiraSettingsResponse update(@RequestBody JiraSettingsRequest request) {
        JiraCredentials candidate = settingsStore.merge(
                request.baseUrl(), request.email(), request.apiToken(), request.jql());
        validator.validate(candidate);
        JiraCredentials updated = settingsStore.apply(candidate);
        return JiraSettingsResponse.from(updated);
    }
}
