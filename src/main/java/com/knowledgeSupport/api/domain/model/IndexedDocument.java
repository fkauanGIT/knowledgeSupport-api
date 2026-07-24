package com.knowledgeSupport.api.domain.model;

import com.knowledgeSupport.api.domain.model.enums.DocumentStatus;
import com.knowledgeSupport.api.domain.model.enums.DocumentType;

import java.time.Instant;
import java.util.UUID;

/**
 * Metadata of an indexed document (manual/PDF/DOCX). The chunks themselves
 * ({@link DocumentChunk}) are a separate concept — most operations (listing, upload
 * response) only need this shell, not the full text.
 */
public class IndexedDocument {
    private UUID id;
    private String name;
    private DocumentType type;
    private int totalChunks;
    private DocumentStatus status;
    private String error;
    private Instant indexedAt;

    private IndexedDocument() {}

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DocumentType getType() {
        return type;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public Instant getIndexedAt() {
        return indexedAt;
    }

    public static class Builder {
        private final IndexedDocument document = new IndexedDocument();

        public Builder id(UUID id) {
            document.id = id;
            return this;
        }

        public Builder name(String name) {
            document.name = name;
            return this;
        }

        public Builder type(DocumentType type) {
            document.type = type;
            return this;
        }

        public Builder totalChunks(int totalChunks) {
            document.totalChunks = totalChunks;
            return this;
        }

        public Builder status(DocumentStatus status) {
            document.status = status;
            return this;
        }

        public Builder error(String error) {
            document.error = error;
            return this;
        }

        public Builder indexedAt(Instant indexedAt) {
            document.indexedAt = indexedAt;
            return this;
        }

        public IndexedDocument build() {
            return document;
        }
    }
}
