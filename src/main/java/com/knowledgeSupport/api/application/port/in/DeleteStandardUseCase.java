package com.knowledgeSupport.api.application.port.in;

import java.util.UUID;

public interface DeleteStandardUseCase {
    void deleteById(UUID id);
}
