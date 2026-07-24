package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.CalledMatch;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "An open Jira ticket this document likely resolves, ranked by relevance")
public record ChamadoRelacionadoResponse(

        @Schema(example = "SUP-4821")
        String chave,

        @Schema(description = "0-100, relative to the best-matching ticket for this document (best = 100)")
        int relevancia) {

    public static ChamadoRelacionadoResponse from(CalledMatch match) {
        return new ChamadoRelacionadoResponse(match.jiraKey(), match.relevance());
    }
}
