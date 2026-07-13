package com.knowledgeSupport.api.adapter.out.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Os campos de uma issue que pedimos ao Jira (query param "fields").
 * "description" chega em ADF (árvore de nós), por isso o tipo JiraDoc.
 * customfield_10432 e customfield_10433 são campos customizados do
 * request type "Erros/Alertas no sistema WINTHOR" (Número Rotina e
 * Nome do Error/Alerta).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraFields(String summary,
                         JiraDoc description,
                         JiraStatus status,
                         JiraIssueType issuetype,
                         JiraReporter reporter,
                         String created,
                         String duedate,
                         String updated,
                         @JsonProperty("customfield_10432") Double routineNumber,
                         @JsonProperty("customfield_10433") JiraDoc errorName) {
}
