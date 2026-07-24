package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.out.DocumentRepositoryPort;
import com.knowledgeSupport.api.domain.model.DocumentChunk;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Índice tokenizado do corpus para a busca TF-IDF. Antes, {@code DocumentSearchService.search}
 * chamava {@code findAllChunks()} (full scan de TODOS os chunks de TODOS os documentos) e
 * re-tokenizava o corpus inteiro A CADA busca — O(total_chunks × chars) por request. Aqui o
 * corpus tokenizado é calculado uma vez e cacheado ("documentCorpus"), invalidado no upload/delete
 * de documento ({@code DocumentService}). Bean separado de propósito: o cache do Spring só
 * intercepta chamadas ENTRE beans, não auto-invocação dentro do mesmo bean.
 */
@Component
public class DocumentCorpusIndex {

    private final DocumentRepositoryPort repository;

    public DocumentCorpusIndex(DocumentRepositoryPort repository) {
        this.repository = repository;
    }

    /** Chunk com seus tokens pré-calculados (a parte cara, agora feita uma vez). */
    public record TokenizedChunk(DocumentChunk chunk, List<String> tokens) {}

    @Cacheable("documentCorpus")
    public List<TokenizedChunk> tokenizedChunks() {
        return repository.findAllChunks().stream()
                .map(chunk -> new TokenizedChunk(chunk, TextTokenizer.tokenize(chunk.text())))
                .toList();
    }
}
