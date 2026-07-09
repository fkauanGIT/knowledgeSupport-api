package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.Standard;

import java.util.Optional;
import java.util.UUID;

public interface GetStandardUseCase {
    Optional<Standard> getById(UUID id);
}
