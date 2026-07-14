package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.RoutineGap;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "How much a routine weighs in the registration gaps")
public record RoutineGapResponse(

        @Schema(description = "Routine number (null = tickets with no routine filled in)", nullable = true)
        Integer routineNumber,

        @Schema(description = "How many tickets from this routine found no Standard")
        int count,

        @Schema(description = "Percentage of the total gaps this routine alone represents")
        double percentageOfGaps,

        @Schema(description = "Example titles, to know what to register")
        List<String> examples) {

    public static RoutineGapResponse from(RoutineGap gap) {
        return new RoutineGapResponse(gap.getRoutineNumber(), gap.getCount(), gap.getPercentageOfGaps(), gap.getExamples());
    }
}
