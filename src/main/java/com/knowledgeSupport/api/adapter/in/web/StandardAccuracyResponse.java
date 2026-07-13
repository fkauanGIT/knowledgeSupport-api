package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.StandardAccuracy;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Taxa de acerto de um Standard, baseada em feedback real registrado")
public record StandardAccuracyResponse(
        UUID standardId,

        @Schema(description = "Quantos feedbacks esse Standard já recebeu")
        int totalFeedbacks,

        @Schema(description = "Quantos desses feedbacks confirmaram que resolveu")
        int resolvedCount,

        @Schema(description = "resolvedCount / totalFeedbacks, 0 quando totalFeedbacks é 0 (sem dado ainda)")
        double accuracyRate) {

    public static StandardAccuracyResponse from(StandardAccuracy accuracy) {
        return new StandardAccuracyResponse(accuracy.getStandardId(), accuracy.getTotalFeedbacks(), accuracy.getResolvedCount(), accuracy.getAccuracyRate());
    }
}
