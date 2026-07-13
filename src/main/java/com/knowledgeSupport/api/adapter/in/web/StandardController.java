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
@Tag(name = "Padrões (Standards)", description = "Catálogo de erros conhecidos e suas soluções — a base de conhecimento do sistema. Persistido em PostgreSQL.")
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
    @Operation(summary = "Cadastra um padrão",
            description = "Registra um novo erro conhecido e sua solução na base de conhecimento.")
    @ApiResponse(responseCode = "201", description = "Padrão criado, devolvido com o id gerado")
    public StandardResponse create(@RequestBody StandardRequest request) {
        Standard created = createStandardUseCase.create(toDomain(request));
        return StandardResponse.from(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um padrão pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Padrão encontrado"),
            @ApiResponse(responseCode = "404", description = "Nenhum padrão com esse id")
    })
    public StandardResponse getById(@PathVariable UUID id) {
        return getStandardUseCase.getById(id)
                .map(StandardResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Standard not found: " + id));
    }

    @GetMapping
    @Operation(summary = "Lista todos os padrões")
    @ApiResponse(responseCode = "200", description = "Lista de padrões cadastrados (pode ser vazia)")
    public List<StandardResponse> listAll() {
        return listStandardsUseCase.listAll().stream().map(StandardResponse::from).toList();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um padrão",
            description = "Substitui os dados do padrão existente pelos enviados no corpo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Padrão atualizado"),
            @ApiResponse(responseCode = "404", description = "Nenhum padrão com esse id")
    })
    public StandardResponse update(@PathVariable UUID id, @RequestBody StandardRequest request) {
        Standard updated = updateStandardUseCase.update(id, toDomain(request));
        return StandardResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um padrão")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Padrão removido"),
            @ApiResponse(responseCode = "404", description = "Nenhum padrão com esse id")
    })
    public void delete(@PathVariable UUID id) {
        deleteStandardUseCase.deleteById(id);
    }

    @GetMapping("/{id}/accuracy")
    @Operation(summary = "Taxa de acerto do padrão, baseada em feedback real",
            description = "Agrega os feedbacks (POST /api/calleds/{key}/feedback) registrados pra esse Standard.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Taxa de acerto"),
            @ApiResponse(responseCode = "404", description = "Nenhum padrão com esse id")
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
