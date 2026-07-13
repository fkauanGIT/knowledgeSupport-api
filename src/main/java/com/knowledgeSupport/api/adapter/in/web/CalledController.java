package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.application.port.in.AnalyzeCalledUseCase;
import com.knowledgeSupport.api.application.port.in.GapReportUseCase;
import com.knowledgeSupport.api.application.port.in.ListCalledsUseCase;
import com.knowledgeSupport.api.application.port.in.SubmitFeedbackUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adapter de entrada (canal web). Não tem POST/PUT/DELETE de propósito:
 * chamados nascem no Jira, não na nossa API. Aqui só consultamos.
 */
@RestController
@RequestMapping("/api/calleds")
@Tag(name = "Chamados (Calleds)", description = "Chamados de suporte consultados ao vivo no Jira — somente leitura. O filtro é configurável via JIRA_JQL no .env.")
public class CalledController {

    private final ListCalledsUseCase listCalledsUseCase;
    private final AnalyzeCalledUseCase analyzeCalledUseCase;
    private final GapReportUseCase gapReportUseCase;
    private final SubmitFeedbackUseCase submitFeedbackUseCase;

    public CalledController(ListCalledsUseCase listCalledsUseCase, AnalyzeCalledUseCase analyzeCalledUseCase,
                            GapReportUseCase gapReportUseCase, SubmitFeedbackUseCase submitFeedbackUseCase) {
        this.listCalledsUseCase = listCalledsUseCase;
        this.analyzeCalledUseCase = analyzeCalledUseCase;
        this.gapReportUseCase = gapReportUseCase;
        this.submitFeedbackUseCase = submitFeedbackUseCase;
    }

    @GetMapping
    @Operation(summary = "Lista os chamados abertos",
            description = "Consulta a API do Jira (projeto SUP) em tempo real e devolve os chamados já traduzidos para o formato do domínio — sem cache e sem persistência local: o Jira é a fonte da verdade.")
    @ApiResponse(responseCode = "200", description = "Lista de chamados abertos (pode ser vazia)")
    public List<CalledResponse> listOpen() {
        return listCalledsUseCase.listOpenCalleds().stream()
                .map(CalledResponse::from)
                .toList();
    }

    @GetMapping("/{key}/analysis")
    @Operation(summary = "Analisa um chamado e tenta encontrar um Standard correspondente",
            description = "Busca o chamado no Jira pela key e compara com os Standards cadastrados pela rotina.")
    @ApiResponse(responseCode = "200", description = "Resultado da análise")
    public CalledAnalysisResponse analyze(@PathVariable String key) {
        return CalledAnalysisResponse.from(analyzeCalledUseCase.analyze(key));
    }

    @GetMapping("/gap-report")
    @Operation(summary = "Relatório de lacunas: onde cadastrar Standard novo rende mais cobertura",
            description = "Roda a análise em todos os chamados abertos e agrupa por rotina os que não encontraram Standard (method NONE), ordenado do maior volume pro menor.")
    @ApiResponse(responseCode = "200", description = "Relatório de lacunas")
    public GapReportResponse gapReport() {
        return GapReportResponse.from(gapReportUseCase.generate());
    }

    @PostMapping("/{key}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra se a sugestão da análise resolveu o chamado",
            description = "Alimenta a taxa de acerto do Standard (GET /api/standards/{id}/accuracy) com um resultado real, não uma suposição.")
    @ApiResponse(responseCode = "201", description = "Feedback registrado")
    public FeedbackResponse submitFeedback(@PathVariable String key, @RequestBody FeedbackRequest request) {
        return FeedbackResponse.from(submitFeedbackUseCase.submit(key, request.standardId(), request.resolved()));
    }
}
