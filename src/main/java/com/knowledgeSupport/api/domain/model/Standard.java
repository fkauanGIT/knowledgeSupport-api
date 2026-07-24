package com.knowledgeSupport.api.domain.model;

import com.knowledgeSupport.api.domain.model.enums.IncidentType;

import java.util.List;
import java.util.UUID;

public class Standard {
    private UUID id;
    private String standardName;
    private String text;
    private String result;
    private IncidentType incidentType;
    private Integer routineNumber;
    private List<InvestigationStep> investigationSteps = List.of();

    private Standard() {}

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getStandardName() {
        return standardName;
    }

    public String getText() {
        return text;
    }

    public String getResult() {
        return result;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public Integer getRoutineNumber() {
        return routineNumber;
    }

    public List<InvestigationStep> getInvestigationSteps() {
        return investigationSteps;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Standard{");
        sb.append("id=").append(id);
        sb.append(", standardName='").append(standardName).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append(", result='").append(result).append('\'');
        sb.append(", incidentType=").append(incidentType);
        sb.append(", routineNumber=").append(routineNumber);
        sb.append(", investigationSteps=").append(investigationSteps);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private final Standard standard = new Standard();

        public Builder id(UUID id) {
            standard.id = id;
            return this;
        }

        public Builder standardName(String standardName) {
            standard.standardName = standardName;
            return this;
        }

        public Builder text(String text) {
            standard.text = text;
            return this;
        }

        public Builder result(String result) {
            standard.result = result;
            return this;
        }

        public Builder incidentType(IncidentType incidentType) {
            standard.incidentType = incidentType;
            return this;
        }

        public Builder routineNumber(Integer routineNumber) {
            standard.routineNumber = routineNumber;
            return this;
        }

        public Builder investigationSteps(List<InvestigationStep> investigationSteps) {
            standard.investigationSteps = investigationSteps == null ? List.of() : investigationSteps;
            return this;
        }

        public Standard build() {
            return standard;
        }
    }
}
