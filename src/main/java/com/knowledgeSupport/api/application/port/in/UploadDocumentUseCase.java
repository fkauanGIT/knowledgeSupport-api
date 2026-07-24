package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.IndexedDocument;

/**
 * Inbound port: extracts, chunks and indexes a document. Synchronous — the caller only
 * gets a response once indexing finished (or failed). A parse failure doesn't throw: it
 * comes back as a normal {@link IndexedDocument} with status FAILED and the error message set.
 */
public interface UploadDocumentUseCase {
    IndexedDocument upload(String filename, byte[] content);
}
