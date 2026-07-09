package com.knowledgeSupport.api.adapter.out.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Os campos de uma issue que pedimos ao Jira (query param "fields").
 * "description" chega em ADF (árvore de nós), por isso o tipo JiraDoc.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraFields(String summary,
                         JiraDoc description,
                         JiraStatus status,
                         JiraReporter reporter,
                         String created,
                         String duedate,
                         String updated) {
}
