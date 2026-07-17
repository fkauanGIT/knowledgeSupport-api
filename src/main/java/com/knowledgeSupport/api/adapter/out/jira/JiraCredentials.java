package com.knowledgeSupport.api.adapter.out.jira;

/**
 * Snapshot imutável das credenciais/config usadas para falar com o Jira.
 * Vive no adapter de saída porque é "assunto Jira" — a mesma fronteira que conhece
 * URL, token e JQL. É trocável em runtime através do {@link JiraSettingsStore}, para
 * que um token expirado possa ser renovado pela interface sem editar o .env nem reiniciar.
 */
public record JiraCredentials(String baseUrl, String email, String apiToken, String jql) {
}
