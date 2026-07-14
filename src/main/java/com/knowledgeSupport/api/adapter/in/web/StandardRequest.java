package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Data to register or update an error pattern")
public record StandardRequest(

        @Schema(description = "Short name identifying the pattern", example = "HUB login permission")
        String standardName,

        @Schema(description = "Description of the error/symptom as it appears in tickets",
                example = "User with buyer profile can't log into the HUB")
        String text,

        @Schema(description = "Standard solution: the step-by-step that fixes the error",
                example = "Add the BUYER profile in the user's access screen")
        String result,

        @Schema(description = "Incident type", example = "ERROR")
        IncidentType incidentType,

        @Schema(description = "WINTHOR routine number (optional)", example = "1234")
        Integer routineNumber
) {
}
