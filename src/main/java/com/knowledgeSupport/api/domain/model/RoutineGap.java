package com.knowledgeSupport.api.domain.model;

import java.util.List;

/**
 * How many tickets from a routine found no Standard, and how much that represents
 * of the total gaps — the answer to "register this here and cover X% of the volume".
 */
public class RoutineGap {
    private final Integer routineNumber; // null = tickets with no routine filled in
    private final int count;
    private final double percentageOfGaps;
    private final List<String> examples;

    public RoutineGap(Integer routineNumber, int count, double percentageOfGaps, List<String> examples) {
        this.routineNumber = routineNumber;
        this.count = count;
        this.percentageOfGaps = percentageOfGaps;
        this.examples = examples;
    }

    public Integer getRoutineNumber() {
        return routineNumber;
    }

    public int getCount() {
        return count;
    }

    public double getPercentageOfGaps() {
        return percentageOfGaps;
    }

    public List<String> getExamples() {
        return examples;
    }
}
