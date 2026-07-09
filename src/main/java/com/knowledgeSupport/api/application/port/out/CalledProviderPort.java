package com.knowledgeSupport.api.application.port.out;

import com.knowledgeSupport.api.domain.model.Called;

import java.util.List;

/**
 * Port de saída: o núcleo declara "preciso de alguém que me forneça chamados".
 * Hoje quem cumpre esse contrato é o JiraCalledAdapter, mas o núcleo não sabe disso.
 */
public interface CalledProviderPort {
    List<Called> fetchOpenCalleds();
}
