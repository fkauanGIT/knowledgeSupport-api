package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.Standard;

import java.util.UUID;

public interface UpdateStandardUseCase {
    Standard update(UUID id, Standard standard);
}
