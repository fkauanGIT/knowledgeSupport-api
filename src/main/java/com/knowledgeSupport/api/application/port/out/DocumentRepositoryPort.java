package com.knowledgeSupport.api.application.port.out;

import com.knowledgeSupport.api.domain.model.DocumentChunk;
import com.knowledgeSupport.api.domain.model.IndexedDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: persistence for indexed documents and their chunks. Chunks are always
 * saved/loaded alongside their parent document — there's no independent chunk lifecycle.
 */
public interface DocumentRepositoryPort {
    IndexedDocument save(IndexedDocument document, List<DocumentChunk> chunks);

    List<IndexedDocument> findAll();

    Optional<IndexedDocument> findById(UUID id);

    /** Ordered by extraction position — the order the text was found in the source file. */
    List<DocumentChunk> findChunksByDocumentId(UUID id);

    /** Every chunk from every INDEXED document — the corpus a search runs against. */
    List<DocumentChunk> findAllChunks();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
