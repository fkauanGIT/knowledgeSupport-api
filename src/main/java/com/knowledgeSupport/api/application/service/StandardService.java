package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.in.CreateStandardUseCase;
import com.knowledgeSupport.api.application.port.in.DeleteStandardUseCase;
import com.knowledgeSupport.api.application.port.in.GetStandardUseCase;
import com.knowledgeSupport.api.application.port.in.ListStandardsUseCase;
import com.knowledgeSupport.api.application.port.in.UpdateStandardUseCase;
import com.knowledgeSupport.api.application.port.out.StandardRepositoryPort;
import com.knowledgeSupport.api.domain.model.Standard;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class StandardService implements CreateStandardUseCase, UpdateStandardUseCase, GetStandardUseCase, ListStandardsUseCase, DeleteStandardUseCase {

    private final StandardRepositoryPort standardRepositoryPort;

    public StandardService(StandardRepositoryPort standardRepositoryPort) {
        this.standardRepositoryPort = standardRepositoryPort;
    }

    @Override
    public Standard create(Standard standard) {
        return standardRepositoryPort.save(standard);
    }

    @Override
    public Standard update(UUID id, Standard standard) {
        if (!standardRepositoryPort.existsById(id)) {
            throw new NoSuchElementException("Standard not found: " + id);
        }
        Standard toSave = Standard.builder()
                .id(id)
                .standardName(standard.getStandardName())
                .text(standard.getText())
                .result(standard.getResult())
                .incidentType(standard.getIncidentType())
                .routineNumber(standard.getRoutineNumber())
                .investigationSteps(standard.getInvestigationSteps())
                .build();
        return standardRepositoryPort.save(toSave);
    }

    @Override
    public Optional<Standard> getById(UUID id) {
        return standardRepositoryPort.findById(id);
    }

    @Override
    public List<Standard> listAll() {
        return standardRepositoryPort.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        if (!standardRepositoryPort.existsById(id)) {
            throw new NoSuchElementException("Standard not found: " + id);
        }
        standardRepositoryPort.deleteById(id);
    }
}
