package com.knowledgeSupport.api.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Date;
import java.util.UUID;

@Entity
// Índice em standard_id: findByStandardId (accuracy) faria full scan conforme feedbacks crescem.
@Table(indexes = @Index(name = "idx_feedback_standard_id", columnList = "standard_id"))
public class FeedbackJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String jiraKey;
    private UUID standardId;
    private boolean resolved;
    private Date createdAt;

    protected FeedbackJpaEntity() {}

    public FeedbackJpaEntity(UUID id, String jiraKey, UUID standardId, boolean resolved, Date createdAt) {
        this.id = id;
        this.jiraKey = jiraKey;
        this.standardId = standardId;
        this.resolved = resolved;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getJiraKey() {
        return jiraKey;
    }

    public void setJiraKey(String jiraKey) {
        this.jiraKey = jiraKey;
    }

    public UUID getStandardId() {
        return standardId;
    }

    public void setStandardId(UUID standardId) {
        this.standardId = standardId;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
