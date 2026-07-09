package com.knowledgeSupport.api.adapter.out.jira;

import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.domain.model.Called;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Adapter de saída: implementa CalledProviderPort falando o idioma do Jira.
 * Tudo que é "assunto Jira" (URL, token, endpoint, JSON) mora aqui.
 * Equivalente ao StandardPersistenceAdapter — só muda o fornecedor:
 * lá é o PostgreSQL, aqui é a API do Jira.
 */
@Component
public class JiraCalledAdapter implements CalledProviderPort {

    private final RestClient restClient;
    private final String jql;

    public JiraCalledAdapter(@Value("${jira.base-url}") String baseUrl,
                             @Value("${jira.email}") String email,
                             @Value("${jira.api-token}") String apiToken,
                             @Value("${jira.jql:created >= -30d ORDER BY created DESC}") String jql) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> headers.setBasicAuth(email, apiToken))
                .build();
        this.jql = jql;
    }

    @Override
    public List<Called> fetchOpenCalleds() {
        JiraSearchResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/3/search/jql")
                        .queryParam("jql", jql)
                        .queryParam("fields", "summary,description,status,reporter,created,duedate,updated")
                        .queryParam("maxResults", 50)
                        .build())
                .retrieve()
                .body(JiraSearchResponse.class);

        if (response == null || response.issues() == null) {
            return List.of();
        }
        return response.issues().stream()
                .map(JiraCalledMapper::toDomain)
                .toList();
    }
}
