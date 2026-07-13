package com.knowledgeSupport.api.domain.model;

public class CalledAnalysis {
    private final Called called;
    private final Standard matchedStandard; // null se não achou
    private final String method;            // "ROUTINE_NUMBER" ou "NONE"

    public CalledAnalysis(Called called, Standard matchedStandard, String method) {
        this.called = called;
        this.matchedStandard = matchedStandard;
        this.method = method;
    }

    public Called getCalled() { return called; }
    public Standard getMatchedStandard() { return matchedStandard; }
    public String getMethod() { return method; }
}
