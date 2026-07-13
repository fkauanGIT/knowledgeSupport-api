package com.knowledgeSupport.api.adapter.out.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraIssueType(String name) {
}
