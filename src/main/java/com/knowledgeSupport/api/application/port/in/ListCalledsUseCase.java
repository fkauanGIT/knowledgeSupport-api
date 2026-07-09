package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.Called;

import java.util.List;

/**
 * Port de entrada: serviço que o núcleo OFERECE ao mundo.
 * Quem chama: adapters de entrada (CalledController hoje; Chatwoot/scheduler no futuro).
 * Quem implementa: CalledService.
 */
public interface ListCalledsUseCase {
    List<Called> listOpenCalleds();
}
