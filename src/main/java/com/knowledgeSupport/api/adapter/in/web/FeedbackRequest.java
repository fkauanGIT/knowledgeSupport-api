package com.knowledgeSupport.api.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Feedback sobre uma sugestão: o Standard indicado resolveu o chamado?")
public record FeedbackRequest(

        @Schema(description = "Id do Standard que foi sugerido (veio de GET /api/calleds/{key}/analysis)")
        UUID standardId,

        @Schema(description = "true se a solução do Standard resolveu o chamado de verdade")
        boolean resolved) {
}
