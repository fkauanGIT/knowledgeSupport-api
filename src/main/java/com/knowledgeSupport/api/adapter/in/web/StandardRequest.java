package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Data to register or update an error pattern")
public record StandardRequest(

        @Schema(description = "Short name identifying the pattern", example = "HUB login permission")
        @NotBlank(message = "standardName é obrigatório")
        @Size(max = 255, message = "standardName excede 255 caracteres")
        String standardName,

        @Schema(description = "Description of the error/symptom as it appears in tickets",
                example = "User with buyer profile can't log into the HUB")
        @Size(max = 20000, message = "text excede 20000 caracteres")
        String text,

        @Schema(description = "Standard solution: the step-by-step that fixes the error",
                example = "Add the BUYER profile in the user's access screen")
        @Size(max = 20000, message = "result excede 20000 caracteres")
        String result,

        @Schema(description = "Incident type", example = "ERROR")
        IncidentType incidentType,

        @Schema(description = "WINTHOR routine number (optional)", example = "1234")
        Integer routineNumber,

        @Schema(description = "Investigation trail behind the solution: hypotheses tested, " +
                "in order, with the query used and what confirmed/discarded each one. Optional.",
                nullable = true)
        @Valid
        List<InvestigationStepRequest> investigationSteps
) {
}
