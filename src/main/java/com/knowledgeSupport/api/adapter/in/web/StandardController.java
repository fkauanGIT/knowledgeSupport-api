package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.application.port.in.CreateStandardUseCase;
import com.knowledgeSupport.api.application.port.in.DeleteStandardUseCase;
import com.knowledgeSupport.api.application.port.in.GetStandardAccuracyUseCase;
import com.knowledgeSupport.api.application.port.in.GetStandardUseCase;
import com.knowledgeSupport.api.application.port.in.ListStandardsUseCase;
import com.knowledgeSupport.api.application.port.in.UpdateStandardUseCase;
import com.knowledgeSupport.api.domain.model.Standard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/standards")
@Tag(name = "Standards", description = "Catalog of known errors and their solutions — the system's knowledge base. Persisted in PostgreSQL.")
public class StandardController {

    private final CreateStandardUseCase createStandardUseCase;
    private final UpdateStandardUseCase updateStandardUseCase;
    private final GetStandardUseCase getStandardUseCase;
    private final ListStandardsUseCase listStandardsUseCase;
    private final DeleteStandardUseCase deleteStandardUseCase;
    private final GetStandardAccuracyUseCase getStandardAccuracyUseCase;

    public StandardController(CreateStandardUseCase createStandardUseCase,
                              UpdateStandardUseCase updateStandardUseCase,
                              GetStandardUseCase getStandardUseCase,
                              ListStandardsUseCase listStandardsUseCase,
                              DeleteStandardUseCase deleteStandardUseCase,
                              GetStandardAccuracyUseCase getStandardAccuracyUseCase) {
        this.createStandardUseCase = createStandardUseCase;
        this.updateStandardUseCase = updateStandardUseCase;
        this.getStandardUseCase = getStandardUseCase;
        this.listStandardsUseCase = listStandardsUseCase;
        this.deleteStandardUseCase = deleteStandardUseCase;
        this.getStandardAccuracyUseCase = getStandardAccuracyUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registers a pattern",
            description = "Registers a new known error and its solution in the knowledge base.")
    @ApiResponse(responseCode = "201", description = "Pattern created, returned with the generated id")
    public StandardResponse create(@RequestBody StandardRequest request) {
        Standard created = createStandardUseCase.create(toDomain(request));
        return StandardResponse.from(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetches a pattern by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pattern found"),
            @ApiResponse(responseCode = "404", description = "No pattern with that id")
    })
    public StandardResponse getById(@PathVariable UUID id) {
        return getStandardUseCase.getById(id)
                .map(StandardResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Standard not found: " + id));
    }

    @GetMapping
    @Operation(summary = "Lists every registered pattern")
    @ApiResponse(responseCode = "200", description = "List of registered patterns (may be empty)")
    public List<StandardResponse> listAll() {
        return listStandardsUseCase.listAll().stream().map(StandardResponse::from).toList();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates a pattern",
            description = "Replaces the existing pattern's data with what's sent in the body.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pattern updated"),
            @ApiResponse(responseCode = "404", description = "No pattern with that id")
    })
    public StandardResponse update(@PathVariable UUID id, @RequestBody StandardRequest request) {
        Standard updated = updateStandardUseCase.update(id, toDomain(request));
        return StandardResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Removes a pattern")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pattern removed"),
            @ApiResponse(responseCode = "404", description = "No pattern with that id")
    })
    public void delete(@PathVariable UUID id) {
        deleteStandardUseCase.deleteById(id);
    }

    @GetMapping("/{id}/accuracy")
    @Operation(summary = "The pattern's accuracy rate, based on real feedback",
            description = "Aggregates the feedback (POST /api/calleds/{key}/feedback) recorded for this Standard.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accuracy rate"),
            @ApiResponse(responseCode = "404", description = "No pattern with that id")
    })
    public StandardAccuracyResponse accuracy(@PathVariable UUID id) {
        return StandardAccuracyResponse.from(getStandardAccuracyUseCase.getAccuracy(id));
    }

    private Standard toDomain(StandardRequest request) {
        return Standard.builder()
                .standardName(request.standardName())
                .text(request.text())
                .result(request.result())
                .incidentType(request.incidentType())
                .routineNumber(request.routineNumber())
                .build();
    }
}
