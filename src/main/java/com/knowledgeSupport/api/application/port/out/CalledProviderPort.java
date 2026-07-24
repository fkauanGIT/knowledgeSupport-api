package com.knowledgeSupport.api.application.port.out;

import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledFilter;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port: the core declares "I need someone to provide me with tickets".
 * Today JiraCalledAdapter fulfills this contract, but the core doesn't know that.
 */
public interface CalledProviderPort {
    List<Called> fetchOpenCalleds(CalledFilter filter);
    Optional<Called> fetchByKey(String key);
}
