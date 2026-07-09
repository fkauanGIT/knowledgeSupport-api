package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.enums.IncidentType;

public record StandardRequest(String standardName, String text, String result, IncidentType incidentType) {
}
