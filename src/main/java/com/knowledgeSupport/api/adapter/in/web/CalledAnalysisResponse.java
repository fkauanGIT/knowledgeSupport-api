package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.CalledAnalysis;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a ticket's automatic analysis")
public record CalledAnalysisResponse(

        @Schema(description = "Ticket title")
        String titleCalled,

        @Schema(description = "Ticket's routine number")
        Integer routineNumber,

        @Schema(description = "Solution found in the matching Standard, if any", nullable = true)
        String solution,

        @Schema(description = "How the solution was found", example = "ROUTINE_AND_TEXT_SCORE")
        String method,

        @Schema(description = "Match confidence, from 0 (none) to 1 (exact match)", example = "0.82")
        double score) {

    public static CalledAnalysisResponse from(CalledAnalysis analysis) {
        return new CalledAnalysisResponse(
                analysis.getCalled().getTitleCalled(),
                analysis.getCalled().getRoutineNumber(),
                analysis.getMatchedStandard() == null ? null : analysis.getMatchedStandard().getResult(),
                analysis.getMethod().getName(),
                analysis.getMethod().getScore()
        );
    }
}
