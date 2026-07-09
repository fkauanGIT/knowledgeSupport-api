package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.application.port.in.CreateStandardUseCase;
import com.knowledgeSupport.api.application.port.in.DeleteStandardUseCase;
import com.knowledgeSupport.api.application.port.in.GetStandardUseCase;
import com.knowledgeSupport.api.application.port.in.ListStandardsUseCase;
import com.knowledgeSupport.api.application.port.in.UpdateStandardUseCase;
import com.knowledgeSupport.api.domain.model.Standard;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/standards")
public class StandardController {

    private final CreateStandardUseCase createStandardUseCase;
    private final UpdateStandardUseCase updateStandardUseCase;
    private final GetStandardUseCase getStandardUseCase;
    private final ListStandardsUseCase listStandardsUseCase;
    private final DeleteStandardUseCase deleteStandardUseCase;

    public StandardController(CreateStandardUseCase createStandardUseCase,
                               UpdateStandardUseCase updateStandardUseCase,
                               GetStandardUseCase getStandardUseCase,
                               ListStandardsUseCase listStandardsUseCase,
                               DeleteStandardUseCase deleteStandardUseCase) {
        this.createStandardUseCase = createStandardUseCase;
        this.updateStandardUseCase = updateStandardUseCase;
        this.getStandardUseCase = getStandardUseCase;
        this.listStandardsUseCase = listStandardsUseCase;
        this.deleteStandardUseCase = deleteStandardUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StandardResponse create(@RequestBody StandardRequest request) {
        Standard created = createStandardUseCase.create(toDomain(request));
        return StandardResponse.from(created);
    }

    @GetMapping("/{id}")
    public StandardResponse getById(@PathVariable UUID id) {
        return getStandardUseCase.getById(id)
                .map(StandardResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Standard not found: " + id));
    }

    @GetMapping
    public List<StandardResponse> listAll() {
        return listStandardsUseCase.listAll().stream().map(StandardResponse::from).toList();
    }

    @PutMapping("/{id}")
    public StandardResponse update(@PathVariable UUID id, @RequestBody StandardRequest request) {
        try {
            Standard updated = updateStandardUseCase.update(id, toDomain(request));
            return StandardResponse.from(updated);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        try {
            deleteStandardUseCase.deleteById(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private Standard toDomain(StandardRequest request) {
        return new Standard(request.standardName(), request.text(), request.result(), request.incidentType());
    }
}
