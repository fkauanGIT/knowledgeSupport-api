package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.StandardAccuracy;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Accuracy rate of a Standard, based on real recorded feedback")
public record StandardAccuracyResponse(
        UUID standardId,

        @Schema(description = "How many feedback entries this Standard has received so far")
        int totalFeedbacks,

        @Schema(description = "How many of those feedback entries confirmed it solved the ticket")
        int resolvedCount,

        @Schema(description = "resolvedCount / totalFeedbacks, 0 when totalFeedbacks is 0 (no data yet)")
        double accuracyRate) {

    public static StandardAccuracyResponse from(StandardAccuracy accuracy) {
        return new StandardAccuracyResponse(accuracy.getStandardId(), accuracy.getTotalFeedbacks(), accuracy.getResolvedCount(), accuracy.getAccuracyRate());
    }
}
