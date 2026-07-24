package com.knowledgeSupport.api.adapter.out.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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

    // Segredo opcional para cifrar o token em disco. Ausente => plaintext (legado/testes). Definido => AES-GCM.
    private static final String ENC_PREFIX = "enc::";
    private final String encryptionSecret = System.getenv("JIRA_SETTINGS_SECRET");

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
            JiraCredentials stored = mapper.readValue(Files.readAllBytes(storePath), JiraCredentials.class);
            return Optional.of(decryptToken(stored));
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
            Files.write(storePath, mapper.writeValueAsBytes(encryptToken(credentials)));
            restrictPermissions(storePath);
        } catch (IOException e) {
            log.error("Falha ao persistir as credenciais do Jira em {}: {}", storePath, e.getMessage());
        }
    }

    // ---- Cifragem em repouso (opt-in via JIRA_SETTINGS_SECRET) e permissões do arquivo --------

    private JiraCredentials encryptToken(JiraCredentials c) {
        if (isBlank(encryptionSecret) || isBlank(c.apiToken()) || c.apiToken().startsWith(ENC_PREFIX)) {
            return c;
        }
        try {
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey(), new GCMParameterSpec(128, iv));
            byte[] ct = cipher.doFinal(c.apiToken().getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            String wrapped = ENC_PREFIX + Base64.getEncoder().encodeToString(out);
            return new JiraCredentials(c.baseUrl(), c.email(), wrapped, c.jql());
        } catch (Exception e) {
            log.error("Falha ao cifrar o token do Jira; abortando a escrita para não gravar em claro.", e);
            throw new IllegalStateException("Falha ao cifrar o token do Jira", e);
        }
    }

    private JiraCredentials decryptToken(JiraCredentials c) {
        String token = c.apiToken();
        if (token == null || !token.startsWith(ENC_PREFIX)) {
            return c;
        }
        if (isBlank(encryptionSecret)) {
            log.error("Token do Jira está cifrado em disco, mas JIRA_SETTINGS_SECRET não está definido; token indisponível.");
            return new JiraCredentials(c.baseUrl(), c.email(), "", c.jql());
        }
        try {
            byte[] all = Base64.getDecoder().decode(token.substring(ENC_PREFIX.length()));
            byte[] iv = java.util.Arrays.copyOfRange(all, 0, 12);
            byte[] ct = java.util.Arrays.copyOfRange(all, 12, all.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey(), new GCMParameterSpec(128, iv));
            String plain = new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
            return new JiraCredentials(c.baseUrl(), c.email(), plain, c.jql());
        } catch (Exception e) {
            log.error("Falha ao decifrar o token do Jira (segredo trocado?); token indisponível.", e);
            return new JiraCredentials(c.baseUrl(), c.email(), "", c.jql());
        }
    }

    private SecretKeySpec aesKey() throws Exception {
        byte[] key = MessageDigest.getInstance("SHA-256").digest(encryptionSecret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, "AES");
    }

    private static void restrictPermissions(Path path) {
        try {
            Files.setPosixFilePermissions(path, EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException | IOException ignored) {
            // FS sem POSIX (ex.: Windows): permissões controladas pelo SO/deploy.
        }
    }

    private static boolean isBlank(String v) {
        return v == null || v.isBlank();
    }
}
