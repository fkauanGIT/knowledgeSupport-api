package com.knowledgeSupport.api.adapter.out.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Mirrors the response of GET /rest/api/3/search/jql.
 * Jira sends a lot more fields; we only declare the ones we use.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraSearchResponse(List<JiraIssuePayload> issues, String nextPageToken) {
}
