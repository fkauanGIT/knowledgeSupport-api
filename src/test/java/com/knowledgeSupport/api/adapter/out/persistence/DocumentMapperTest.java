package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.domain.model.DocumentChunk;
import com.knowledgeSupport.api.domain.model.IndexedDocument;
import com.knowledgeSupport.api.domain.model.enums.DocumentStatus;
import com.knowledgeSupport.api.domain.model.enums.DocumentType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DocumentMapperTest {

    @Test
    void toDomainAndToEntity_roundTripWithoutLosingAnyField() {
        UUID id = UUID.randomUUID();
        Instant indexedAt = Instant.parse("2026-07-23T18:00:00Z");
        IndexedDocument original = IndexedDocument.builder()
                .id(id)
                .name("Manual WinThor.pdf")
                .type(DocumentType.PDF)
                .totalChunks(3)
                .status(DocumentStatus.INDEXED)
                .indexedAt(indexedAt)
                .build();

        DocumentJpaEntity entity = DocumentMapper.toEntity(original);
        IndexedDocument backToDomain = DocumentMapper.toDomain(entity);

        assertEquals(id, backToDomain.getId());
        assertEquals("Manual WinThor.pdf", backToDomain.getName());
        assertEquals(DocumentType.PDF, backToDomain.getType());
        assertEquals(3, backToDomain.getTotalChunks());
        assertEquals(DocumentStatus.INDEXED, backToDomain.getStatus());
        assertNull(backToDomain.getError());
        assertEquals(indexedAt, backToDomain.getIndexedAt());
    }

    @Test
    void toDomainAndToEntity_failedDocument_keepsErrorMessage() {
        IndexedDocument original = IndexedDocument.builder()
                .id(UUID.randomUUID())
                .name("relatorio.pdf")
                .type(DocumentType.PDF)
                .totalChunks(0)
                .status(DocumentStatus.FAILED)
                .error("PDF corrompido")
                .indexedAt(Instant.now())
                .build();

        DocumentJpaEntity entity = DocumentMapper.toEntity(original);
        IndexedDocument backToDomain = DocumentMapper.toDomain(entity);

        assertEquals(DocumentStatus.FAILED, backToDomain.getStatus());
        assertEquals("PDF corrompido", backToDomain.getError());
    }

    @Test
    void chunk_toDomainAndToEntity_roundTrip() {
        UUID documentId = UUID.randomUUID();
        DocumentChunk original = new DocumentChunk(documentId, "Manual.pdf", 5, "trecho de texto", 2);

        DocumentChunkJpaEntity entity = DocumentMapper.toEntityChunk(original);
        DocumentChunk backToDomain = DocumentMapper.toDomainChunk(entity, "Manual.pdf");

        assertEquals(documentId, backToDomain.documentId());
        assertEquals("Manual.pdf", backToDomain.documentName());
        assertEquals(5, backToDomain.page());
        assertEquals("trecho de texto", backToDomain.text());
        assertEquals(2, backToDomain.position());
    }
}
