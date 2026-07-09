package com.knowledgeSupport.api.domain.model;

import java.util.UUID;

public class Requester {
    private UUID id;
    private String requesterName;
    private Integer filialRequester;
    private String requesterNumber;

    protected Requester() {}

    public Requester(UUID id, String requesterName, Integer filialRequester, String requesterNumber) {
        this.id = id;
        this.requesterName = requesterName;
        this.filialRequester = filialRequester;
        this.requesterNumber = requesterNumber;
    }

    public Requester(String requesterName, Integer filialRequester, String requesterNumber) {
        this(null, requesterName, filialRequester, requesterNumber);
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

    public Integer getFilialRequester() {
        return filialRequester;
    }

    public void setFilialRequester(Integer filialRequester) {
        this.filialRequester = filialRequester;
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
        sb.append(", filialRequester=").append(filialRequester);
        sb.append(", requesterNumber='").append(requesterNumber).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
