package com.knowledgeSupport.api.adapter.out.jira;

import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.domain.model.Called;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter de saída: implementa CalledProviderPort falando o idioma do Jira.
 * Tudo que é "assunto Jira" (URL, token, endpoint, JSON) mora aqui.
 * Equivalente ao StandardPersistenceAdapter — só muda o fornecedor:
 * lá é o PostgreSQL, aqui é a API do Jira.
 */
@Component
public class JiraCalledAdapter implements CalledProviderPort {

    private static final Logger log = LoggerFactory.getLogger(JiraCalledAdapter.class);
    private static final String FIELDS = "summary,description,status,issuetype,reporter,created,duedate,updated,customfield_10432,customfield_10433";
    private static final int MAX_RESULTS_PER_PAGE = 50;
    private static final int MAX_PAGES = 20; // trava de segurança: 20 x 50 = 1000 chamados por listagem
    private static final int MAX_RETRIES_429 = 3;

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
        List<Called> calleds = new ArrayList<>();
        String pageToken = null;

        for (int page = 0; page < MAX_PAGES; page++) {
            JiraSearchResponse response = fetchPageWithRetry(pageToken);
            if (response == null || response.issues() == null) {
                break;
            }

            response.issues().stream().map(JiraCalledMapper::toDomain).forEach(calleds::add);

            pageToken = response.nextPageToken();
            if (pageToken == null || response.issues().size() < MAX_RESULTS_PER_PAGE) {
                break;
            }
        }

        return calleds;
    }

    @Override
    public Optional<Called> fetchByKey(String key) {
        try {
            JiraIssuePayload issue = restClient.get()
                    .uri("/rest/api/3/issue/{key}?fields=" + FIELDS, key)
                    .retrieve()
                    .body(JiraIssuePayload.class);

            return Optional.ofNullable(issue).map(JiraCalledMapper::toDomain);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    private JiraSearchResponse fetchPageWithRetry(String pageToken) {
        for (int attempt = 1; attempt <= MAX_RETRIES_429; attempt++) {
            try {
                return fetchPage(pageToken);
            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt == MAX_RETRIES_429) {
                    log.warn("Jira retornou 429 (rate limit) {} vezes seguidas; devolvendo os chamados ja coletados ate aqui.", attempt);
                    return null;
                }
                long waitMillis = retryAfterMillis(e).orElse(1000L * attempt);
                log.warn("Jira retornou 429 (rate limit), tentativa {}/{}; aguardando {}ms.", attempt, MAX_RETRIES_429, waitMillis);
                sleep(waitMillis);
            }
        }
        return null;
    }

    private JiraSearchResponse fetchPage(String pageToken) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/rest/api/3/search/jql")
                            .queryParam("jql", jql)
                            .queryParam("fields", FIELDS)
                            .queryParam("maxResults", MAX_RESULTS_PER_PAGE);
                    if (pageToken != null) {
                        uriBuilder.queryParam("nextPageToken", pageToken);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(JiraSearchResponse.class);
    }

    private Optional<Long> retryAfterMillis(HttpClientErrorException.TooManyRequests e) {
        String retryAfter = e.getResponseHeaders() == null ? null : e.getResponseHeaders().getFirst("Retry-After");
        if (retryAfter == null) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(retryAfter.trim()) * 1000);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
