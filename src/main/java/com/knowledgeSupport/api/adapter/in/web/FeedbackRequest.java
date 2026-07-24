package com.knowledgeSupport.api.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Feedback on a suggestion: did the indicated Standard solve the ticket?")
public record FeedbackRequest(

        @Schema(description = "Id of the Standard that was suggested (came from GET /api/calleds/{key}/analysis)")
        @NotNull(message = "standardId é obrigatório")
        UUID standardId,

        @Schema(description = "true if the Standard's solution actually solved the ticket")
        boolean resolved) {
}
