package com.knowledgeSupport.api.adapter.out.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fonte única das credenciais do Jira em tempo de execução.
 *
 * <p>Por que existe: antes o token vinha só do {@code .env} via {@code @Value} e era
 * "queimado" no construtor do {@link JiraCalledAdapter}. Quando o token do Atlassian
 * expirava, era preciso editar o arquivo e reiniciar a aplicação. Este store torna as
 * credenciais mutáveis: o valor do {@code .env} é apenas a semente inicial e pode ser
 * sobrescrito em runtime (via {@code PUT /api/settings/jira}), com o override persistido
 * em disco para sobreviver a reinícios.</p>
 *
 * <p>Precedência na inicialização: se existir o arquivo de override, ele vence o
 * {@code .env} (foi o último valor definido pelo operador). Caso contrário, usa o
 * {@code .env}.</p>
 */
@Component
public class JiraSettingsStore {

    private static final Logger log = LoggerFactory.getLogger(JiraSettingsStore.class);

    private final Path storePath;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicReference<JiraCredentials> current;

    public JiraSettingsStore(@Value("${jira.base-url:}") String baseUrl,
                             @Value("${jira.email:}") String email,
                             @Value("${jira.api-token:}") String apiToken,
                             @Value("${jira.jql:created >= -30d ORDER BY created DESC}") String jql,
                             @Value("${jira.api-token-expires-at:}") String apiTokenExpiresAt,
                             @Value("${jira.settings-file:./jira-settings.json}") String settingsFile) {
        this.storePath = Path.of(settingsFile);
        JiraCredentials fromEnv = new JiraCredentials(baseUrl, email, apiToken, jql);
        Optional<JiraCredentials> override = loadOverride();
        this.current = new AtomicReference<>(override.orElse(fromEnv));
        override.ifPresent(c ->
                log.info("Credenciais do Jira carregadas do override em disco ({}).", storePath.toAbsolutePath()));
        logTokenExpiry(apiTokenExpiresAt);
    }

    /** Snapshot atual das credenciais. Nunca é null. */
    public JiraCredentials current() {
        return current.get();
    }

    /**
     * Calcula, a partir dos valores enviados, quais credenciais passariam a valer — sem
     * persistir nada. Campos em branco/nulos preservam o valor atual (em especial o token
     * pode ser omitido para trocar só a URL/JQL sem reenviar o segredo). Existe separado de
     * {@link #apply} para permitir validar o candidato (chamando o Jira de verdade) antes de
     * gravar em disco.
     */
    public synchronized JiraCredentials merge(String baseUrl, String email, String apiToken, String jql) {
        JiraCredentials prev = current.get();
        return new JiraCredentials(
                keepIfBlank(baseUrl, prev.baseUrl()),
                keepIfBlank(email, prev.email()),
                keepIfBlank(apiToken, prev.apiToken()),
                keepIfBlank(jql, prev.jql()));
    }

    /** Efetiva um candidato (normalmente vindo de {@link #merge}) e persiste o override em disco. */
    public synchronized JiraCredentials apply(JiraCredentials next) {
        current.set(next);
        persist(next);
        return next;
    }

    /** Atalho para {@code apply(merge(...))} — usado onde não há validação prévia a fazer. */
    public synchronized JiraCredentials update(String baseUrl, String email, String apiToken, String jql) {
        return apply(merge(baseUrl, email, apiToken, jql));
    }

    private void logTokenExpiry(String expiresAt) {
        if (expiresAt == null || expiresAt.isBlank()) {
            return;
        }
        try {
            LocalDate expiry = LocalDate.parse(expiresAt.trim());
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiry);
            if (daysLeft < 0) {
                log.warn("O token da API do Jira EXPIROU em {} — configure um novo via PUT /api/settings/jira.", expiry);
            } else if (daysLeft <= 30) {
                log.warn("O token da API do Jira expira em {} dias ({}) — considere renová-lo em breve.", daysLeft, expiry);
            } else {
                log.info("Token da API do Jira configurado, válido até {} ({} dias restantes).", expiry, daysLeft);
            }
        } catch (DateTimeParseException e) {
            log.warn("JIRA_API_TOKEN_EXPIRES_AT='{}' não é uma data válida (use AAAA-MM-DD); aviso de expiração desativado.",
                    expiresAt);
        }
    }

    private static String keepIfBlank(String incoming, String fallback) {
        return (incoming == null || incoming.isBlank()) ? fallback : incoming;
    }

    private Optional<JiraCredentials> loadOverride() {
        try {
            if (!Files.exists(storePath)) {
                return Optional.empty();
            }
            return Optional.of(mapper.readValue(Files.readAllBytes(storePath), JiraCredentials.class));
        } catch (IOException e) {
            log.warn("Não foi possível ler o override de credenciais do Jira em {}: {}", storePath, e.getMessage());
            return Optional.empty();
        }
    }

    private void persist(JiraCredentials credentials) {
        try {
            Path parent = storePath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(storePath, mapper.writeValueAsBytes(credentials));
        } catch (IOException e) {
            log.error("Falha ao persistir as credenciais do Jira em {}: {}", storePath, e.getMessage());
        }
    }
}
