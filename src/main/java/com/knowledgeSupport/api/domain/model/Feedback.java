package com.knowledgeSupport.api.domain.model;

import java.util.Date;
import java.util.UUID;

/**
 * Resultado de uma sugestão: o analista confirma se o Standard sugerido resolveu o
 * chamado de verdade. Base pra "taxa de acerto por Standard" (GetStandardAccuracyUseCase) —
 * confiança auditável, diferente de uma IA que só "parece confiante".
 */
public class Feedback {
    private final UUID id;
    private final String jiraKey;
    private final UUID standardId;
    private final boolean resolved;
    private final Date createdAt;

    public Feedback(UUID id, String jiraKey, UUID standardId, boolean resolved, Date createdAt) {
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

    public UUID getStandardId() {
        return standardId;
    }

    public boolean isResolved() {
        return resolved;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
