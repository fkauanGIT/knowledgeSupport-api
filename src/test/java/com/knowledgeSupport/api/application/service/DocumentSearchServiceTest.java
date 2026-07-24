package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.application.port.out.DocumentRepositoryPort;
import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledFilter;
import com.knowledgeSupport.api.domain.model.CalledMatch;
import com.knowledgeSupport.api.domain.model.ChunkMatch;
import com.knowledgeSupport.api.domain.model.DocumentChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentSearchServiceTest {

    @Mock
    private DocumentRepositoryPort documentRepository;

    @Mock
    private CalledProviderPort calledProviderPort;

    @Mock
    private DocumentCorpusIndex corpusIndex;

    private DocumentSearchService service() {
        return new DocumentSearchService(documentRepository, calledProviderPort, corpusIndex);
    }

    private static DocumentCorpusIndex.TokenizedChunk tokenized(DocumentChunk chunk) {
        return new DocumentCorpusIndex.TokenizedChunk(chunk, TextTokenizer.tokenize(chunk.text()));
    }

    @Test
    void search_ranksChunksByRelevanceAndCapsAtThree() {
        UUID docId = UUID.randomUUID();
        when(corpusIndex.tokenizedChunks()).thenReturn(List.of(
                tokenized(new DocumentChunk(docId, "manual", 1, "erro sefaz 866 ausencia de troco", 0)),
                tokenized(new DocumentChunk(docId, "manual", 2, "nota fiscal pendente de autorizacao", 1)),
                tokenized(new DocumentChunk(docId, "manual", 3, "sefaz rejeicao 866", 2)),
                tokenized(new DocumentChunk(docId, "manual", 4, "pedido de compra cancelado", 3))
        ));

        List<ChunkMatch> matches = service().search("rejeicao 866 sefaz");

        assertTrue(matches.size() <= 3);
        assertTrue(matches.get(0).relevance() >= matches.get(matches.size() - 1).relevance());
        // The chunk with no overlapping terms at all must not appear.
        assertTrue(matches.stream().noneMatch(m -> m.text().equals("pedido de compra cancelado")));
    }

    @Test
    void search_withNoIndexedChunks_returnsEmptyList() {
        when(corpusIndex.tokenizedChunks()).thenReturn(List.of());

        assertEquals(List.of(), service().search("qualquer coisa"));
    }

    @Test
    void relatedCalleds_documentNotFound_throws() {
        UUID id = UUID.randomUUID();
        when(documentRepository.existsById(id)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> service().relatedCalleds(id));
    }

    @Test
    void relatedCalleds_ranksOpenTicketsAgainstTheDocumentsText_excludingZeroScores() {
        UUID id = UUID.randomUUID();
        when(documentRepository.existsById(id)).thenReturn(true);
        when(documentRepository.findChunksByDocumentId(id)).thenReturn(List.of(
                new DocumentChunk(id, "manual", 1, "rejeicao sefaz 866 ausencia de troco na nota fiscal", 0)
        ));
        Called matching = Called.builder().jiraKey("SUP-1").titleCalled("Rejeicao 866 sefaz").build();
        Called unrelated = Called.builder().jiraKey("SUP-2").titleCalled("Impressora sem tinta").build();
        when(calledProviderPort.fetchOpenCalleds(CalledFilter.NONE)).thenReturn(List.of(matching, unrelated));

        List<CalledMatch> matches = service().relatedCalleds(id);

        assertEquals(1, matches.size());
        assertEquals("SUP-1", matches.get(0).jiraKey());
        assertEquals(100, matches.get(0).relevance());
    }

    @Test
    void relatedCalleds_documentWithNoChunks_returnsEmptyList() {
        UUID id = UUID.randomUUID();
        when(documentRepository.existsById(id)).thenReturn(true);
        when(documentRepository.findChunksByDocumentId(id)).thenReturn(List.of());

        assertEquals(List.of(), service().relatedCalleds(id));
    }
}
