package com.knowledgeSupport.api.application.port.in;

import java.util.UUID;

public interface DeleteDocumentUseCase {
    /** @throws java.util.NoSuchElementException if no document exists with that id */
    void deleteById(UUID id);
}
