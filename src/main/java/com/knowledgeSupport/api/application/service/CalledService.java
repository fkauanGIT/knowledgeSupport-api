package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.in.ListCalledsUseCase;
import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.domain.model.Called;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Core: for now it just passes through, but this is WHERE the product's central
 * business rule will live — comparing the Called's errorName/incidentType against the
 * Standards to suggest the standard solution (future AnalyzeCalledUseCase).
 * Notice: no import of HTTP, Jira, or the database. Only ports and domain.
 */
@Service
public class CalledService implements ListCalledsUseCase {

    private final CalledProviderPort calledProviderPort;

    public CalledService(CalledProviderPort calledProviderPort) {
        this.calledProviderPort = calledProviderPort;
    }

    @Override
    public List<Called> listOpenCalleds() {
        return calledProviderPort.fetchOpenCalleds();
    }
}
