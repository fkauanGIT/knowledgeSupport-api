package com.knowledgeSupport.api.domain.model;

import com.knowledgeSupport.api.domain.model.enums.FilterCategory;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
public class Called {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String titleCalled;
    private String descriptionCalled;
    private String errorName;

    @Enumerated(EnumType.STRING)
    private IncidentType incidentType;
    @Enumerated(EnumType.STRING)
    private FilterCategory filterCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Requester requester;
    private Date createdAt;
    private Date deadline;
    private Date updateAt;

    protected Called() {}

    public Called(String titleCalled, String descriptionCalled, String errorName, IncidentType incidentType, FilterCategory filterCategory, Requester requester, Date createdAt, Date deadline, Date updateAt) {
        this.titleCalled = titleCalled;
        this.descriptionCalled = descriptionCalled;
        this.errorName = errorName;
        this.incidentType = incidentType;
        this.filterCategory = filterCategory;
        this.requester = requester;
        this.createdAt = createdAt;
        this.deadline = deadline;
        this.updateAt = updateAt;
    }
}