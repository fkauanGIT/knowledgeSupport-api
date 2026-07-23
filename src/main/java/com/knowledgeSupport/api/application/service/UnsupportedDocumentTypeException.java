package com.knowledgeSupport.api.application.service;

/** Thrown for an upload whose filename isn't .pdf or .docx — a client input error (400), not a parse failure. */
public class UnsupportedDocumentTypeException extends RuntimeException {
    public UnsupportedDocumentTypeException(String message) {
        super(message);
    }
}
