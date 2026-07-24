package com.knowledgeSupport.api.adapter.out.jira;

import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledFilter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Outbound adapter: implements CalledProviderPort speaking Jira's language.
 * Everything that's "Jira business" (URL, token, endpoint, JSON) lives here.
 * Equivalent to StandardPersistenceAdapter — only the provider changes:
 * there it's PostgreSQL, here it's the Jira API.
 */
@Component
public class JiraCalledAdapter implements CalledProviderPort {

    private static final Logger log = LoggerFactory.getLogger(JiraCalledAdapter.class);
    private static final String FIELDS = "summary,description,status,issuetype,reporter,assignee,created,duedate,updated,customfield_10432,customfield_10433";
    private static final int MAX_RESULTS_PER_PAGE = 50;
    private static final int MAX_PAGES = 20; // safety cap: 20 x 50 = 1000 tickets per listing
    private static final int MAX_RETRIES_429 = 3;
    private static final Pattern ORDER_BY = Pattern.compile("(?i)\\border\\s+by\\b");

    // Um Jira pendurado NÃO pode segurar a thread do Tomcat para sempre: sem timeout, uma conexão
    // travada esgota o pool sob carga. 2s para conectar, 8s para ler.
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(8);

    private final JiraSettingsStore settingsStore;
    private final Timer fetchTimer;

    // Cache do RestClient: reconstruído só quando as credenciais mudam (hot-swap do token).
    private volatile JiraCredentials cachedCredentials;
    private volatile RestClient cachedClient;

    public JiraCalledAdapter(JiraSettingsStore settingsStore, MeterRegistry meterRegistry) {
        this.settingsStore = settingsStore;
        this.fetchTimer = Timer.builder("jira.fetch")
                .description("Latência das chamadas à API do Jira")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    /**
     * Devolve um RestClient alinhado com as credenciais atuais do store, reconstruindo-o
     * apenas quando elas mudaram (ex.: token renovado via PUT /api/settings/jira).
     */
    private RestClient restClient() {
        JiraCredentials credentials = settingsStore.current();
        if (cachedClient == null || !credentials.equals(cachedCredentials)) {
            synchronized (this) {
                if (cachedClient == null || !credentials.equals(cachedCredentials)) {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(CONNECT_TIMEOUT);
                    factory.setReadTimeout(READ_TIMEOUT);
                    cachedClient = RestClient.builder()
                            .requestFactory(factory)
                            .baseUrl(credentials.baseUrl())
                            .defaultHeaders(headers -> headers.setBasicAuth(credentials.email(), credentials.apiToken()))
                            .build();
                    cachedCredentials = credentials;
                }
            }
        }
        return cachedClient;
    }

    /** Chave de cache do fetchOpenCalleds: muda quando a JQL configurada muda (config em runtime). */
    public String jqlCacheKey() {
        return settingsStore.current().jql();
    }

    /**
     * Layers the filter's clauses (created window, only-open) on top of whatever JQL is
     * already configured, instead of replacing it. Slices the configured JQL at "ORDER BY"
     * first, since new AND-clauses can't be appended after an ORDER BY.
     */
    private String buildJql(CalledFilter filter) {
        String base = settingsStore.current().jql();
        Matcher matcher = ORDER_BY.matcher(base);

        String where;
        String orderBy;
        if (matcher.find()) {
            where = base.substring(0, matcher.start()).trim();
            orderBy = base.substring(matcher.start()).trim();
        } else {
            where = base.trim();
            orderBy = "";
        }

        List<String> clauses = new ArrayList<>();
        if (!where.isEmpty()) {
            clauses.add("(" + where + ")");
        }
        if (filter.createdFrom() != null) {
            clauses.add("created >= \"" + filter.createdFrom() + "\"");
        }
        if (filter.createdTo() != null) {
            clauses.add("created < \"" + filter.createdTo().plusDays(1) + "\"");
        }
        if (Boolean.TRUE.equals(filter.onlyOpen())) {
            clauses.add("statusCategory != Done");
        } else if (Boolean.FALSE.equals(filter.onlyOpen())) {
            clauses.add("statusCategory = Done");
        }

        String combinedWhere = String.join(" AND ", clauses);
        if (combinedWhere.isEmpty()) {
            return orderBy;
        }
        return orderBy.isEmpty() ? combinedWhere : combinedWhere + " " + orderBy;
    }

    @Override
    @Cacheable(cacheNames = "openCalleds", key = "#root.target.jqlCacheKey() + '|' + #filter")
    public List<Called> fetchOpenCalleds(CalledFilter filter) {
        return fetchTimer.record(() -> {
            List<Called> calleds = new ArrayList<>();
            String jql = buildJql(filter);
            String pageToken = null;

            for (int page = 0; page < MAX_PAGES; page++) {
                String token = pageToken;
                JiraSearchResponse response = withRetry(() -> fetchPage(jql, token));
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
        });
    }

    @Override
    public Optional<Called> fetchByKey(String key) {
        return fetchTimer.record(() -> {
            try {
                JiraIssuePayload issue = withRetry(() -> restClient().get()
                        .uri("/rest/api/3/issue/{key}?fields=" + FIELDS, key)
                        .retrieve()
                        .body(JiraIssuePayload.class));

                return Optional.ofNullable(issue).map(JiraCalledMapper::toDomain);
            } catch (HttpClientErrorException.NotFound e) {
                return Optional.empty();
            }
        });
    }

    /**
     * Política única de resiliência: repete em 429 com backoff (respeitando Retry-After).
     * Antes só a listagem tinha retry; o fetchByKey (caminho quente por ticket) estourava 429 cru.
     */
    private <T> T withRetry(Supplier<T> call) {
        for (int attempt = 1; attempt <= MAX_RETRIES_429; attempt++) {
            try {
                return call.get();
            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt == MAX_RETRIES_429) {
                    log.warn("Jira retornou 429 (rate limit) {} vezes seguidas; desistindo desta chamada.", attempt);
                    return null;
                }
                long waitMillis = retryAfterMillis(e).orElse(1000L * attempt);
                log.warn("Jira retornou 429 (rate limit), tentativa {}/{}; aguardando {}ms.", attempt, MAX_RETRIES_429, waitMillis);
                sleep(waitMillis);
            }
        }
        return null;
    }

    private JiraSearchResponse fetchPage(String jql, String pageToken) {
        return restClient().get()
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
