package com.knowledgeSupport.api.domain.model;

/** A Jira ticket a document likely resolves, with relevance normalized to 0-100. */
public record CalledMatch(String jiraKey, int relevance) {
}
