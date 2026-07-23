package com.knowledgeSupport.api.adapter.out.jira;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class JiraSettingsStoreTest {

    private static final String JQL = "created >= -30d ORDER BY created DESC";

    private JiraSettingsStore storeAt(Path file, String baseUrl, String email, String token) {
        return new JiraSettingsStore(baseUrl, email, token, JQL, "", file.toString());
    }

    @Test
    void seedsFromEnvWhenNoOverrideExists(@TempDir Path dir) {
        JiraSettingsStore store = storeAt(dir.resolve("s.json"), "https://env.atlassian.net", "env@x.com", "env-token");

        JiraCredentials current = store.current();

        assertThat(current.baseUrl()).isEqualTo("https://env.atlassian.net");
        assertThat(current.email()).isEqualTo("env@x.com");
        assertThat(current.apiToken()).isEqualTo("env-token");
        assertThat(current.jql()).isEqualTo(JQL);
    }

    @Test
    void updateReplacesAndPersists(@TempDir Path dir) {
        Path file = dir.resolve("s.json");
        JiraSettingsStore store = storeAt(file, "https://env.atlassian.net", "env@x.com", "env-token");

        store.update("https://new.atlassian.net", "new@x.com", "new-token", "ORDER BY created ASC");

        assertThat(store.current().baseUrl()).isEqualTo("https://new.atlassian.net");
        assertThat(store.current().apiToken()).isEqualTo("new-token");
        assertThat(Files.exists(file)).isTrue();
    }

    @Test
    void blankTokenKeepsCurrentToken(@TempDir Path dir) {
        JiraSettingsStore store = storeAt(dir.resolve("s.json"), "https://env.atlassian.net", "env@x.com", "env-token");

        store.update("https://new.atlassian.net", "new@x.com", "  ", null);

        assertThat(store.current().apiToken()).isEqualTo("env-token");
        assertThat(store.current().baseUrl()).isEqualTo("https://new.atlassian.net");
        assertThat(store.current().jql()).isEqualTo(JQL);
    }

    @Test
    void persistedOverrideTakesPrecedenceOverEnvOnRestart(@TempDir Path dir) {
        Path file = dir.resolve("s.json");
        storeAt(file, "https://env.atlassian.net", "env@x.com", "env-token")
                .update("https://override.atlassian.net", "override@x.com", "override-token", JQL);

        // Novo store (simula reinício): deve carregar o override, não o .env.
        JiraSettingsStore afterRestart = storeAt(file, "https://env.atlassian.net", "env@x.com", "env-token");

        assertThat(afterRestart.current().baseUrl()).isEqualTo("https://override.atlassian.net");
        assertThat(afterRestart.current().apiToken()).isEqualTo("override-token");
    }
}
