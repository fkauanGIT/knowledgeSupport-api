package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.domain.model.DocumentChunk;
import com.knowledgeSupport.api.domain.model.IndexedDocument;

public final class DocumentMapper {

    private DocumentMapper() {}

    public static IndexedDocument toDomain(DocumentJpaEntity entity) {
        return IndexedDocument.builder()
                .id(entity.getId())
                .name(entity.getNome())
                .type(entity.getTipo())
                .totalChunks(entity.getTotalTrechos())
                .status(entity.getStatus())
                .error(entity.getErro())
                .indexedAt(entity.getIndexadoEm())
                .build();
    }

    public static DocumentJpaEntity toEntity(IndexedDocument document) {
        return new DocumentJpaEntity(document.getId(), document.getName(), document.getType(),
                document.getTotalChunks(), document.getStatus(), document.getError(), document.getIndexedAt());
    }

    public static DocumentChunk toDomainChunk(DocumentChunkJpaEntity entity, String documentName) {
        return new DocumentChunk(entity.getDocumentoId(), documentName, entity.getPagina(), entity.getTexto(),
                entity.getPosicao());
    }

    public static DocumentChunkJpaEntity toEntityChunk(DocumentChunk chunk) {
        return new DocumentChunkJpaEntity(chunk.documentId(), chunk.page(), chunk.text(), chunk.position());
    }
}
