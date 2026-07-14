package com.knowledgeSupport.api.domain.model;

import com.knowledgeSupport.api.domain.model.enums.Confidence;

public final class MatchMethod {

    private final String name;
    private final double score;
    private final Confidence confidence;

    private MatchMethod(String name, double score, Confidence confidence) {
        this.name = name;
        this.score = score;
        this.confidence = confidence;
    }

    public static MatchMethod none() {
        return new MatchMethod("NONE", 0.0, Confidence.NONE);
    }

    public static MatchMethod of(String name, double score, Confidence confidence) {
        return new MatchMethod(name, score, confidence);
    }

    public String getName() {
        return name;
    }

    public double getScore() {
        return score;
    }

    public Confidence getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return name + "(" + score + ", " + confidence + ")";
    }
}
