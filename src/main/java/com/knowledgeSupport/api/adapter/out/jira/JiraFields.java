package com.knowledgeSupport.api.adapter.out.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The issue fields we ask Jira for (the "fields" query param).
 * "description" arrives as ADF (a tree of nodes), hence the JiraDoc type.
 * customfield_10432 and customfield_10433 are custom fields on the
 * "Errors/Alerts in the WINTHOR system" request type (labeled "Número Rotina"
 * and "Nome do Error/Alerta" in the actual Jira instance).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraFields(String summary,
                         JiraDoc description,
                         JiraStatus status,
                         JiraIssueType issuetype,
                         JiraReporter reporter,
                         JiraReporter assignee,
                         String created,
                         String duedate,
                         String updated,
                         @JsonProperty("customfield_10432") Double routineNumber,
                         @JsonProperty("customfield_10433") JiraDoc errorName) {
}
