package com.knowledgeSupport.api.domain.model;

import java.util.UUID;

/**
 * One indexed slice of a document's text (~700 chars, word-boundary safe). {@code page} is
 * null for DOCX (no real pagination in the XML) and set for PDF (one "page text" per chunk
 * source — a page can still split into multiple chunks if it's long). {@code documentName}
 * is denormalized on purpose: search results need it and it never changes independently of
 * the document, so carrying it here avoids a join/lookup on every search.
 */
public record DocumentChunk(UUID documentId, String documentName, Integer page, String text, int position) {
}
