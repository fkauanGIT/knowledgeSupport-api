package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.InvestigationStep;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One step of the investigation trail behind a Standard's solution")
public record InvestigationStepResponse(

        @Schema(description = "What was suspected as the cause", example = "Fiscal configuration out of sync for the branch")
        String hypothesis,

        @Schema(description = "SQL/table checked to test the hypothesis", nullable = true,
                example = "SELECT * FROM pcconfig WHERE codfilial = :branch")
        String query,

        @Schema(description = "What was found, and whether it confirmed or discarded the hypothesis",
                example = "Config row was missing for the branch — confirmed")
        String verification,

        @Schema(description = "Whether this step turned out to be the actual root cause", example = "true")
        boolean confirmed
) {
    public static InvestigationStepResponse from(InvestigationStep step) {
        return new InvestigationStepResponse(step.getHypothesis(), step.getQuery(), step.getVerification(), step.isConfirmed());
    }
}
