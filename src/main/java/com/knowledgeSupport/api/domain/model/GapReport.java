package com.knowledgeSupport.api.domain.model;

import java.util.List;

/**
 * Aggregated result of running the analysis over every open ticket and looking only
 * at the ones that came back NONE — where registering a new Standard yields the most coverage.
 */
public class GapReport {
    private final int totalCalledsAnalyzed;
    private final int totalWithoutMatch;
    private final List<RoutineGap> gapsByRoutine; // sorted by count desc

    public GapReport(int totalCalledsAnalyzed, int totalWithoutMatch, List<RoutineGap> gapsByRoutine) {
        this.totalCalledsAnalyzed = totalCalledsAnalyzed;
        this.totalWithoutMatch = totalWithoutMatch;
        this.gapsByRoutine = gapsByRoutine;
    }

    public int getTotalCalledsAnalyzed() {
        return totalCalledsAnalyzed;
    }

    public int getTotalWithoutMatch() {
        return totalWithoutMatch;
    }

    public List<RoutineGap> getGapsByRoutine() {
        return gapsByRoutine;
    }
}
