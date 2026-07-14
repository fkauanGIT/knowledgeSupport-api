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

        @Schema(description = "Match score, from 0 (none) to 1 (exact match)", example = "0.82")
        double score,

        @Schema(description = "How much to trust this result: CONFIRMED (exact match, safe to " +
                "apply automatically), LIKELY (strong text match, still worth a glance), " +
                "UNCERTAIN (cleared the minimum threshold but it's a guess — review before " +
                "trusting it), NONE (no match found)", example = "LIKELY")
        String confidence) {

    public static CalledAnalysisResponse from(CalledAnalysis analysis) {
        return new CalledAnalysisResponse(
                analysis.getCalled().getTitleCalled(),
                analysis.getCalled().getRoutineNumber(),
                analysis.getMatchedStandard() == null ? null : analysis.getMatchedStandard().getResult(),
                analysis.getMethod().getName(),
                analysis.getMethod().getScore(),
                analysis.getMethod().getConfidence().name()
        );
    }
}
