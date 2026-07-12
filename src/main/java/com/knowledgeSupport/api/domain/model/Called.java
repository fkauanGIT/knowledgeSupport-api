package com.knowledgeSupport.api.domain.model;

import com.knowledgeSupport.api.domain.model.enums.FilterCategory;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;

import java.util.Date;
import java.util.UUID;

public class Called {
    private UUID id;
    private String titleCalled;
    private String descriptionCalled;
    private Integer routineNumber;
    private String errorName;
    private IncidentType incidentType;
    private FilterCategory filterCategory;
    private Requester requester;
    private Date createdAt;
    private Date deadline;
    private Date updateAt;

    protected Called() {}

    public Called(UUID id, String titleCalled, String descriptionCalled, String errorName, IncidentType incidentType,
                  FilterCategory filterCategory, Requester requester, Date createdAt, Date deadline, Date updateAt, Integer routineNumber) {
        this.id = id;
        this.titleCalled = titleCalled;
        this.descriptionCalled = descriptionCalled;
        this.routineNumber = routineNumber;
        this.errorName = errorName;
        this.incidentType = incidentType;
        this.filterCategory = filterCategory;
        this.requester = requester;
        this.createdAt = createdAt;
        this.deadline = deadline;
        this.updateAt = updateAt;
    }

    public Called(String titleCalled, String descriptionCalled, String errorName, IncidentType incidentType, FilterCategory filterCategory, Requester requester, Date createdAt, Date deadline, Date updateAt, Integer routineNumber) {
        this(null, titleCalled, descriptionCalled, errorName, incidentType, filterCategory, requester, createdAt, deadline, updateAt, routineNumber);
    }

    public UUID getId() {
        return id;
    }

    public String getTitleCalled() {
        return titleCalled;
    }

    public void setTitleCalled(String titleCalled) {
        this.titleCalled = titleCalled;
    }

    public String getDescriptionCalled() {
        return descriptionCalled;
    }

    public void setDescriptionCalled(String descriptionCalled) {
        this.descriptionCalled = descriptionCalled;
    }

    public String getErrorName() {
        return errorName;
    }

    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(IncidentType incidentType) {
        this.incidentType = incidentType;
    }

    public FilterCategory getFilterCategory() {
        return filterCategory;
    }

    public void setFilterCategory(FilterCategory filterCategory) {
        this.filterCategory = filterCategory;
    }

    public Requester getRequester() {
        return requester;
    }

    public void setRequester(Requester requester) {
        this.requester = requester;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    public Integer getRoutineNumber() {return routineNumber;}

    public void setRoutineNumber(Integer routineNumber) {this.routineNumber = routineNumber;}

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Called{");
        sb.append("id=").append(id);
        sb.append(", titleCalled='").append(titleCalled).append('\'');
        sb.append(", descriptionCalled='").append(descriptionCalled).append('\'');
        sb.append(", errorName='").append(errorName).append('\'');
        sb.append(", errorName='").append(errorName).append('\'');
        sb.append(", incidentType=").append(incidentType);
        sb.append(", filterCategory=").append(filterCategory);
        sb.append(", requester=").append(requester);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", deadline=").append(deadline);
        sb.append(", updateAt=").append(updateAt);
        sb.append('}');
        return sb.toString();
    }
}
