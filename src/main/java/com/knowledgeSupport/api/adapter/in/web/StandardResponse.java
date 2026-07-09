package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.Standard;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;

import java.util.UUID;

public record StandardResponse(UUID id, String standardName, String text, String result, IncidentType incidentType) {

    public static StandardResponse from(Standard standard) {
        return new StandardResponse(standard.getId(), standard.getStandardName(), standard.getText(), standard.getResult(), standard.getIncidentType());
    }
}
