package com.knowledgeSupport.api.adapter.out.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * A node of the Atlassian Document Format (ADF) — Jira's rich-text format.
 * The structure is recursive: a doc contains paragraphs, which contain text nodes.
 * We're only interested in extracting the plain text (see JiraCalledMapper.extractText).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraDoc(String type, String text, List<JiraDoc> content) {
}
