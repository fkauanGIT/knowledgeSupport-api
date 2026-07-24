package com.knowledgeSupport.api.domain.model;

import java.util.UUID;

/** A chunk found by a search, with its relevance already normalized to 0-100 (best match = 100). */
public record ChunkMatch(UUID documentId, String documentName, Integer page, String text, int relevance) {
}
