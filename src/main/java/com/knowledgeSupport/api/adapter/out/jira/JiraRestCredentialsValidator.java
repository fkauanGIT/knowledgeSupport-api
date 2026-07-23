package com.knowledgeSupport.api.adapter.out.jira;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Validates a candidate Jira config with a real, cheap call (maxResults=1) against
 * /rest/api/3/search/jql — the same endpoint {@link JiraCalledAdapter} uses to list tickets.
 * Never persists anything itself; that's the settings store's job once this succeeds.
 */
@Component
public class JiraRestCredentialsValidator implements JiraCredentialsValidator {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void validate(JiraCredentials candidate) {
        try {
            RestClient.builder()
                    .baseUrl(candidate.baseUrl())
                    .defaultHeaders(headers -> headers.setBasicAuth(candidate.email(), candidate.apiToken()))
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/rest/api/3/search/jql")
                            .queryParam("jql", candidate.jql())
                            .queryParam("maxResults", 1)
                            .build())
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            throw new JiraCredentialsInvalidException(extractMessage(e), e);
        } catch (RestClientException e) {
            throw new JiraCredentialsInvalidException(
                    "Não foi possível conectar ao Jira em " + candidate.baseUrl() + ": " + e.getMessage(), e);
        }
    }

    private String extractMessage(HttpClientErrorException e) {
        try {
            JsonNode body = mapper.readTree(e.getResponseBodyAsByteArray());
            JsonNode errorMessages = body.get("errorMessages");
            if (errorMessages != null && errorMessages.isArray() && !errorMessages.isEmpty()) {
                return errorMessages.get(0).asText();
            }
        } catch (Exception ignored) {
            // Corpo não era o JSON de erro esperado do Jira; cai no fallback abaixo.
        }
        return "Jira recusou a configuração informada (HTTP " + e.getStatusCode().value() + ").";
    }
}
