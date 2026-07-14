package com.knowledgeSupport.api.domain.model;

public final class InvestigationStep {

    private String hypothesis;
    private String query;
    private String verification;
    private boolean confirmed;

    private InvestigationStep() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getHypothesis() {
        return hypothesis;
    }

    public String getQuery() {
        return query;
    }

    public String getVerification() {
        return verification;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public static class Builder {
        private final InvestigationStep step = new InvestigationStep();

        public Builder hypothesis(String hypothesis) {
            step.hypothesis = hypothesis;
            return this;
        }

        public Builder query(String query) {
            step.query = query;
            return this;
        }

        public Builder verification(String verification) {
            step.verification = verification;
            return this;
        }

        public Builder confirmed(boolean confirmed) {
            step.confirmed = confirmed;
            return this;
        }

        public InvestigationStep build() {
            return step;
        }
    }
}
