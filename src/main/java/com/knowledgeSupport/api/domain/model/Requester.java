package com.knowledgeSupport.api.domain.model;

import java.util.UUID;

public class Requester {
    private UUID id;
    private String requesterName;
    private Integer requesterBranch;
    private String requesterNumber;

    protected Requester() {}

    public Requester(UUID id, String requesterName, Integer requesterBranch, String requesterNumber) {
        this.id = id;
        this.requesterName = requesterName;
        this.requesterBranch = requesterBranch;
        this.requesterNumber = requesterNumber;
    }

    public Requester(String requesterName, Integer requesterBranch, String requesterNumber) {
        this(null, requesterName, requesterBranch, requesterNumber);
    }

    public UUID getId() {
        return id;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public Integer getRequesterBranch() {
        return requesterBranch;
    }

    public void setRequesterBranch(Integer requesterBranch) {
        this.requesterBranch = requesterBranch;
    }

    public String getRequesterNumber() {
        return requesterNumber;
    }

    public void setRequesterNumber(String requesterNumber) {
        this.requesterNumber = requesterNumber;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Requester{");
        sb.append("id=").append(id);
        sb.append(", requesterName='").append(requesterName).append('\'');
        sb.append(", requesterBranch=").append(requesterBranch);
        sb.append(", requesterNumber='").append(requesterNumber).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
