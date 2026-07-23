package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.adapter.out.jira.JiraCredentialsInvalidException;
import com.knowledgeSupport.api.adapter.out.jira.JiraCredentialsValidator;
import com.knowledgeSupport.api.adapter.out.jira.JiraSettingsStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JiraSettingsControllerTest {

    private static final String JQL = "created >= -30d ORDER BY created DESC";
    private static final JiraCredentialsValidator ACCEPT_ALL = candidate -> { };

    private JiraSettingsController controllerWith(Path dir) {
        return controllerWith(dir, ACCEPT_ALL);
    }

    private JiraSettingsController controllerWith(Path dir, JiraCredentialsValidator validator) {
        JiraSettingsStore store = new JiraSettingsStore(
                "https://env.atlassian.net", "env@x.com", "env-token", JQL, "", dir.resolve("s.json").toString());
        return new JiraSettingsController(store, validator);
    }

    @Test
    void getNeverExposesTokenButReportsItIsConfigured(@TempDir Path dir) {
        JiraSettingsResponse response = controllerWith(dir).get();

        assertThat(response.baseUrl()).isEqualTo("https://env.atlassian.net");
        assertThat(response.email()).isEqualTo("env@x.com");
        assertThat(response.jql()).isEqualTo(JQL);
        assertThat(response.tokenConfigured()).isTrue();
    }

    @Test
    void updateChangesConfigAndReportsTokenStillConfigured(@TempDir Path dir) {
        JiraSettingsController controller = controllerWith(dir);

        JiraSettingsResponse response = controller.update(
                new JiraSettingsRequest("https://new.atlassian.net", "new@x.com", "new-token", "ORDER BY created ASC"));

        assertThat(response.baseUrl()).isEqualTo("https://new.atlassian.net");
        assertThat(response.email()).isEqualTo("new@x.com");
        assertThat(response.jql()).isEqualTo("ORDER BY created ASC");
        assertThat(response.tokenConfigured()).isTrue();
    }

    @Test
    void tokenConfiguredIsFalseWhenNoToken(@TempDir Path dir) {
        JiraSettingsStore store = new JiraSettingsStore(
                "https://env.atlassian.net", "env@x.com", "", JQL, "", dir.resolve("s.json").toString());

        JiraSettingsResponse response = new JiraSettingsController(store, ACCEPT_ALL).get();

        assertThat(response.tokenConfigured()).isFalse();
    }

    @Test
    void updateRejectsInvalidCredentialsAndDoesNotPersist(@TempDir Path dir) {
        JiraCredentialsValidator rejectAll = candidate -> {
            throw new JiraCredentialsInvalidException("JQL inválida", null);
        };
        JiraSettingsController controller = controllerWith(dir, rejectAll);

        assertThatThrownBy(() -> controller.update(
                new JiraSettingsRequest("", "", "", "SUP ORDER BY created DESC")))
                .isInstanceOf(JiraCredentialsInvalidException.class);

        assertThat(controller.get().jql()).isEqualTo(JQL);
    }
}
