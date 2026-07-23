package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.DocumentChunk;

import java.util.List;
import java.util.UUID;

public interface GetDocumentChunksUseCase {
    /** @throws java.util.NoSuchElementException if no document exists with that id */
    List<DocumentChunk> chunksOf(UUID documentId);
}
