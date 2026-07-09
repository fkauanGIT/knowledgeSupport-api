package com.knowledgeSupport.api.domain.model;

import com.knowledgeSupport.api.domain.model.enums.IncidentType;

import java.util.UUID;

public class Standard {
    private UUID id;
    private String standardName;
    private String text;
    private String result;
    private IncidentType incidentType;

    protected Standard() {}

    public Standard(UUID id, String standardName, String text, String result, IncidentType incidentType) {
        this.id = id;
        this.standardName = standardName;
        this.text = text;
        this.result = result;
        this.incidentType = incidentType;
    }

    public Standard(String standardName, String text, String result, IncidentType incidentType) {
        this(null, standardName, text, result, incidentType);
    }

    public UUID getId() {
        return id;
    }

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = standardName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(IncidentType incidentType) {
        this.incidentType = incidentType;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Standard{");
        sb.append("id=").append(id);
        sb.append(", standardName='").append(standardName).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append(", result='").append(result).append('\'');
        sb.append(", incidentType=").append(incidentType);
        sb.append('}');
        return sb.toString();
    }
}
