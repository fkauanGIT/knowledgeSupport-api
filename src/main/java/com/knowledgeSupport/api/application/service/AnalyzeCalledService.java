package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.in.AnalyzeCalledUseCase;
import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.application.port.out.StandardRepositoryPort;
import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledAnalysis;
import com.knowledgeSupport.api.domain.model.Standard;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AnalyzeCalledService implements AnalyzeCalledUseCase {

    private final CalledProviderPort calledProviderPort;
    private final StandardRepositoryPort standardRepositoryPort;

    public AnalyzeCalledService(CalledProviderPort calledProviderPort, StandardRepositoryPort standardRepositoryPort) {
        this.calledProviderPort = calledProviderPort;
        this.standardRepositoryPort = standardRepositoryPort;
    }

    @Override
    public CalledAnalysis analyze(String jiraKey) {
        Called called = calledProviderPort.fetchByKey(jiraKey)
                .orElseThrow(() -> new NoSuchElementException("Called not found: " + jiraKey));

        List<Standard> standards = standardRepositoryPort.findAll();

        for (Standard standard : standards) {
            if (mesmaRotinaEMesmoErro(called, standard)) {
                return new CalledAnalysis(called, standard, "ROUTINE_AND_ERROR_NAME");
            }
        }

        return new CalledAnalysis(called, null, "NONE");
    }

    private boolean mesmaRotinaEMesmoErro(Called called, Standard standard) {
        boolean rotinaBate = called.getRoutineNumber() != null
                && called.getRoutineNumber().equals(standard.getRoutineNumber());

        boolean erroBate = called.getErrorName() != null
                && standard.getStandardName() != null
                && called.getErrorName().trim().equalsIgnoreCase(standard.getStandardName().trim());

        boolean temSolucao = standard.getResult() != null
                && !standard.getResult().isBlank();

        return rotinaBate && erroBate && temSolucao;
    }
}