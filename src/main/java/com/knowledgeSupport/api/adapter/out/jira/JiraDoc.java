package com.knowledgeSupport.api.adapter.out.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Nó do Atlassian Document Format (ADF) — o formato de texto rico do Jira.
 * A estrutura é recursiva: um doc contém parágrafos, que contêm nós de texto.
 * Só nos interessa extrair o texto puro (ver JiraCalledMapper.extractText).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraDoc(String type, String text, List<JiraDoc> content) {
}
