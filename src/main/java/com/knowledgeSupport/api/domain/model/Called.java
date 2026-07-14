package com.knowledgeSupport.api.domain.model;

import com.knowledgeSupport.api.domain.model.enums.FilterCategory;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;

import java.util.Date;
import java.util.UUID;

public class Called {
    private UUID id;
    private String jiraKey;
    private String titleCalled;
    private String descriptionCalled;
    private Integer routineNumber;
    private String errorName;
    private IncidentType incidentType;
    private FilterCategory filterCategory;
    private String status;
    private Requester requester;
    private Date createdAt;
    private Date deadline;
    private Date updateAt;

    private Called() {}

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getJiraKey() {
        return jiraKey;
    }

    public String getTitleCalled() {
        return titleCalled;
    }

    public String getDescriptionCalled() {
        return descriptionCalled;
    }

    public String getErrorName() {
        return errorName;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public FilterCategory getFilterCategory() {
        return filterCategory;
    }

    public String getStatus() {
        return status;
    }

    public Requester getRequester() {
        return requester;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getDeadline() {
        return deadline;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public Integer getRoutineNumber() {
        return routineNumber;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Called{");
        sb.append("id=").append(id);
        sb.append(", jiraKey='").append(jiraKey).append('\'');
        sb.append(", titleCalled='").append(titleCalled).append('\'');
        sb.append(", descriptionCalled='").append(descriptionCalled).append('\'');
        sb.append(", routineNumber=").append(routineNumber);
        sb.append(", errorName='").append(errorName).append('\'');
        sb.append(", incidentType=").append(incidentType);
        sb.append(", filterCategory=").append(filterCategory);
        sb.append(", status='").append(status).append('\'');
        sb.append(", requester=").append(requester);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", deadline=").append(deadline);
        sb.append(", updateAt=").append(updateAt);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Called has 13 fields: a positional constructor was a source of silent bugs
     * (swapping two Date/String arguments compiles fine). The builder
     * names each field at the call site.
     */
    public static class Builder {
        private final Called called = new Called();

        public Builder id(UUID id) {
            called.id = id;
            return this;
        }

        public Builder jiraKey(String jiraKey) {
            called.jiraKey = jiraKey;
            return this;
        }

        public Builder titleCalled(String titleCalled) {
            called.titleCalled = titleCalled;
            return this;
        }

        public Builder descriptionCalled(String descriptionCalled) {
            called.descriptionCalled = descriptionCalled;
            return this;
        }

        public Builder routineNumber(Integer routineNumber) {
            called.routineNumber = routineNumber;
            return this;
        }

        public Builder errorName(String errorName) {
            called.errorName = errorName;
            return this;
        }

        public Builder incidentType(IncidentType incidentType) {
            called.incidentType = incidentType;
            return this;
        }

        public Builder filterCategory(FilterCategory filterCategory) {
            called.filterCategory = filterCategory;
            return this;
        }

        public Builder status(String status) {
            called.status = status;
            return this;
        }

        public Builder requester(Requester requester) {
            called.requester = requester;
            return this;
        }

        public Builder createdAt(Date createdAt) {
            called.createdAt = createdAt;
            return this;
        }

        public Builder deadline(Date deadline) {
            called.deadline = deadline;
            return this;
        }

        public Builder updateAt(Date updateAt) {
            called.updateAt = updateAt;
            return this;
        }

        public Called build() {
            return called;
        }
    }
}
