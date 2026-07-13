package com.knowledgeSupport.api.domain.model;

public final class MatchMethod {

    private final String name;
    private final double score;

    private MatchMethod(String name, double score) {
        this.name = name;
        this.score = score;
    }

    public static MatchMethod none() {
        return new MatchMethod("NONE", 0.0);
    }

    public static MatchMethod of(String name, double score) {
        return new MatchMethod(name, score);
    }

    public String getName() {
        return name;
    }

    public double getScore() {
        return score;
    }

    @Override
    public String toString() {
        return name + "(" + score + ")";
    }
}
