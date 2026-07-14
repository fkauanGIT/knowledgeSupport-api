package com.knowledgeSupport.api.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class InvestigationStepEmbeddable {

    @Column(columnDefinition = "text")
    private String hypothesis;

    @Column(columnDefinition = "text")
    private String query;

    @Column(columnDefinition = "text")
    private String verification;

    private boolean confirmed;

    protected InvestigationStepEmbeddable() {}

    public InvestigationStepEmbeddable(String hypothesis, String query, String verification, boolean confirmed) {
        this.hypothesis = hypothesis;
        this.query = query;
        this.verification = verification;
        this.confirmed = confirmed;
    }

    public String getHypothesis() {
        return hypothesis;
    }

    public void setHypothesis(String hypothesis) {
        this.hypothesis = hypothesis;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
