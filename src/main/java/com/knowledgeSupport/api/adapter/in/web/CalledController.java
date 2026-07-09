package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.application.port.in.ListCalledsUseCase;
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
public class CalledController {

    private final ListCalledsUseCase listCalledsUseCase;

    public CalledController(ListCalledsUseCase listCalledsUseCase) {
        this.listCalledsUseCase = listCalledsUseCase;
    }

    @GetMapping
    public List<CalledResponse> listOpen() {
        return listCalledsUseCase.listOpenCalleds().stream()
                .map(CalledResponse::from)
                .toList();
    }
}
