package com.knowledgeSupport.api.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Requester {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String requesterName;
    private Integer filialRequester;
    private String requesterNumber;

    protected Requester() {}

    public Requester(String requesterName, Integer filialRequester, String requesterNumber) {
        this.requesterName = requesterName;
        this.filialRequester = filialRequester;
        this.requesterNumber = requesterNumber;
    }
}
