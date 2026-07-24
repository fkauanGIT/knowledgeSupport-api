package com.knowledgeSupport.api.domain.model;

import java.util.UUID;

public class Requester {
    private UUID id;
    private String requesterName;
    private String requesterNumber;

    protected Requester() {}

    public Requester(UUID id, String requesterName, String requesterNumber) {
        this.id = id;
        this.requesterName = requesterName;
        this.requesterNumber = requesterNumber;
    }

    public Requester(String requesterName, String requesterNumber) {
        this(null, requesterName, requesterNumber);
    }

    public UUID getId() { return id; }

    public String getRequesterName() { return requesterName; }

    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public String getRequesterNumber() { return requesterNumber; }

    public void setRequesterNumber(String requesterNumber) { this.requesterNumber = requesterNumber; }

    @Override
    public String toString() {
        // Sem dados de contato (e-mail/telefone): evita vazar PII se um Called/Requester for logado.
        return "Requester{id=" + id + ", requesterName='" + requesterName + "'}";
    }
}
