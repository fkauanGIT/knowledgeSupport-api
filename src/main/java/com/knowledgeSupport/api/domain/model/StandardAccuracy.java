package com.knowledgeSupport.api.domain.model;

import java.util.UUID;

public class StandardAccuracy {
    private final UUID standardId;
    private final int totalFeedbacks;
    private final int resolvedCount;
    private final double accuracyRate; // 0-1; 0 when totalFeedbacks == 0 (no data, not "0% accuracy")

    public StandardAccuracy(UUID standardId, int totalFeedbacks, int resolvedCount, double accuracyRate) {
        this.standardId = standardId;
        this.totalFeedbacks = totalFeedbacks;
        this.resolvedCount = resolvedCount;
        this.accuracyRate = accuracyRate;
    }

    public UUID getStandardId() {
        return standardId;
    }

    public int getTotalFeedbacks() {
        return totalFeedbacks;
    }

    public int getResolvedCount() {
        return resolvedCount;
    }

    public double getAccuracyRate() {
        return accuracyRate;
    }
}
