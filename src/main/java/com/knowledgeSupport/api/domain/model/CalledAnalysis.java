package com.knowledgeSupport.api.domain.model;

public class CalledAnalysis {
    private final Called called;
    private final Standard matchedStandard; // null if none was found
    private final MatchMethod method;

    public CalledAnalysis(Called called, Standard matchedStandard, MatchMethod method) {
        this.called = called;
        this.matchedStandard = matchedStandard;
        this.method = method;
    }

    public Called getCalled() { return called; }
    public Standard getMatchedStandard() { return matchedStandard; }
    public MatchMethod getMethod() { return method; }
}
