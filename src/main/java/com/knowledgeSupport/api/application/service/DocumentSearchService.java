package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.in.RelatedCalledsUseCase;
import com.knowledgeSupport.api.application.port.in.SearchChunksUseCase;
import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.application.port.out.DocumentRepositoryPort;
import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledFilter;
import com.knowledgeSupport.api.domain.model.CalledMatch;
import com.knowledgeSupport.api.domain.model.ChunkMatch;
import com.knowledgeSupport.api.domain.model.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Crosses the TF-IDF search over two different corpora, both against the same document's
 * text: {@link #search} ranks chunks (across every document) against a free-text query;
 * {@link #relatedCalleds} flips it — ranks open Jira tickets against ONE document's full
 * text, to answer "which tickets does this manual probably resolve".
 */
@Service
public class DocumentSearchService implements SearchChunksUseCase, RelatedCalledsUseCase {

    private static final int SEARCH_MAX_RESULTS = 3;

    private final DocumentRepositoryPort documentRepository;
    private final CalledProviderPort calledProviderPort;

    public DocumentSearchService(DocumentRepositoryPort documentRepository, CalledProviderPort calledProviderPort) {
        this.documentRepository = documentRepository;
        this.calledProviderPort = calledProviderPort;
    }

    @Override
    public List<ChunkMatch> search(String query) {
        List<DocumentChunk> chunks = documentRepository.findAllChunks();
        if (chunks.isEmpty()) {
            return List.of();
        }

        List<String> queryTokens = TextTokenizer.tokenize(query);
        List<List<String>> corpus = chunks.stream().map(c -> TextTokenizer.tokenize(c.text())).toList();
        double[] scores = TfIdfSearch.score(corpus, queryTokens);
        int[] relevances = TfIdfSearch.toRelevancePercentages(scores);

        return rankedIndices(scores)
                .limit(SEARCH_MAX_RESULTS)
                .mapToObj(i -> toChunkMatch(chunks.get(i), relevances[i]))
                .toList();
    }

    @Override
    public List<CalledMatch> relatedCalleds(UUID documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new NoSuchElementException("Documento não encontrado: " + documentId);
        }
        List<DocumentChunk> documentChunks = documentRepository.findChunksByDocumentId(documentId);
        if (documentChunks.isEmpty()) {
            return List.of();
        }

        String fullText = documentChunks.stream().map(DocumentChunk::text).collect(Collectors.joining(" "));
        List<String> documentTokens = TextTokenizer.tokenize(fullText);

        List<Called> calleds = calledProviderPort.fetchOpenCalleds(CalledFilter.NONE);
        if (calleds.isEmpty()) {
            return List.of();
        }

        List<List<String>> corpus = calleds.stream().map(this::tokenizeCalled).toList();
        double[] scores = TfIdfSearch.score(corpus, documentTokens);
        int[] relevances = TfIdfSearch.toRelevancePercentages(scores);

        return rankedIndices(scores)
                .mapToObj(i -> new CalledMatch(calleds.get(i).getJiraKey(), relevances[i]))
                .toList();
    }

    private List<String> tokenizeCalled(Called called) {
        String title = called.getTitleCalled() == null ? "" : called.getTitleCalled();
        String errorName = called.getErrorName() == null ? "" : called.getErrorName();
        return TextTokenizer.tokenize((title + " " + errorName).trim());
    }

    /** Indices of entries with a positive score, sorted best-first. */
    private IntStream rankedIndices(double[] scores) {
        return IntStream.range(0, scores.length)
                .filter(i -> scores[i] > 0)
                .boxed()
                .sorted(Comparator.<Integer>comparingDouble(i -> scores[i]).reversed())
                .mapToInt(Integer::intValue);
    }

    private ChunkMatch toChunkMatch(DocumentChunk chunk, int relevance) {
        return new ChunkMatch(chunk.documentId(), chunk.documentName(), chunk.page(), chunk.text(), relevance);
    }
}
