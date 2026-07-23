package com.knowledgeSupport.api.application.port.out;

/**
 * Thrown when a file can't be parsed (corrupted, password-protected, ...). Deliberately NOT
 * thrown for a file that opens fine but yields no text (a scanned PDF with no text layer) —
 * that's a valid "indexed with 0 chunks" outcome, not a failure.
 */
public class DocumentExtractionException extends RuntimeException {
    public DocumentExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
