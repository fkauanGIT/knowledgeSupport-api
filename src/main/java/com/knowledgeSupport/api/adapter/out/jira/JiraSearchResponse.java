package com.knowledgeSupport.api.adapter.out.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Espelha a resposta do GET /rest/api/3/search/jql.
 * O Jira manda muito mais campos; só declaramos o que usamos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraSearchResponse(List<JiraIssuePayload> issues, String nextPageToken) {
}
