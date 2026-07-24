package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.ChunkMatch;

import java.util.List;

/**
 * Free-text search across every indexed document's chunks (TF-IDF, keyword — not embeddings).
 * Used when analyzing a ticket: the query is its title + errorName.
 */
public interface SearchChunksUseCase {
    /** Top 3 matches, sorted by relevance desc. Empty if nothing scored above zero. */
    List<ChunkMatch> search(String query);
}
