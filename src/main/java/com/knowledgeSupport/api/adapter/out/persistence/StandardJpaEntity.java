package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
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

    // No separate JPA entity/repository: a step has no identity or lifecycle outside its
    // Standard, same precedent as Requester living inside Called (see ARCHITECTURE.md).
    // LAZY: o matcher (caminho quente) nem usa os passos; @BatchSize evita o N+1 ao carregá-los.
    @ElementCollection(fetch = FetchType.LAZY)
    @BatchSize(size = 200)
    @CollectionTable(name = "standard_investigation_step", joinColumns = @JoinColumn(name = "standard_id"))
    @OrderColumn(name = "step_order")
    private List<InvestigationStepEmbeddable> investigationSteps = new ArrayList<>();

    protected StandardJpaEntity() {}

    public StandardJpaEntity(UUID id, String standardName, String text, String result, IncidentType incidentType,
                              Integer routineNumber, List<InvestigationStepEmbeddable> investigationSteps) {
        this.id = id;
        this.standardName = standardName;
        this.text = text;
        this.result = result;
        this.incidentType = incidentType;
        this.routineNumber = routineNumber;
        this.investigationSteps = investigationSteps == null ? new ArrayList<>() : new ArrayList<>(investigationSteps);
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

    public List<InvestigationStepEmbeddable> getInvestigationSteps() {
        return investigationSteps;
    }

    public void setInvestigationSteps(List<InvestigationStepEmbeddable> investigationSteps) {
        this.investigationSteps = investigationSteps == null ? new ArrayList<>() : new ArrayList<>(investigationSteps);
    }
}
