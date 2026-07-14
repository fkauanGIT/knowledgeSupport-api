package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class StandardJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String standardName;

    // no 255 limit: the text accumulates symptom variations over time (see LIMITATIONS.md)
    @Column(columnDefinition = "text")
    private String text;

    @Column(columnDefinition = "text")
    private String result;

    private Integer routineNumber;

    @Enumerated(EnumType.STRING)
    private IncidentType incidentType;

    protected StandardJpaEntity() {}

    public StandardJpaEntity(UUID id, String standardName, String text, String result, IncidentType incidentType, Integer routineNumber) {
        this.id = id;
        this.standardName = standardName;
        this.text = text;
        this.result = result;
        this.incidentType = incidentType;
        this.routineNumber = routineNumber;
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

    public Integer getRoutineNumber() { return routineNumber; }

    public void setRoutineNumber(Integer routineNumber) {this.routineNumber = routineNumber; }
}
