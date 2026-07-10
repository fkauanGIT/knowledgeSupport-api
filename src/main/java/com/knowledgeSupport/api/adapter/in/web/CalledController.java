package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.application.port.in.ListCalledsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    public CalledController(ListCalledsUseCase listCalledsUseCase) {
        this.listCalledsUseCase = listCalledsUseCase;
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
}
