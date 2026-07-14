package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.Called;

import java.util.List;

/**
 * Inbound port: a service the core OFFERS to the world.
 * Called by: inbound adapters (CalledController today; Chatwoot/scheduler in the future).
 * Implemented by: CalledService.
 */
public interface ListCalledsUseCase {
    List<Called> listOpenCalleds();
}
