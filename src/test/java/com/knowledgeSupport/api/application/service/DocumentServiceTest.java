package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.out.DocumentExtractionException;
import com.knowledgeSupport.api.application.port.out.DocumentRepositoryPort;
import com.knowledgeSupport.api.application.port.out.DocumentTextExtractorPort;
import com.knowledgeSupport.api.application.port.out.ExtractedPage;
import com.knowledgeSupport.api.domain.model.DocumentChunk;
import com.knowledgeSupport.api.domain.model.IndexedDocument;
import com.knowledgeSupport.api.domain.model.enums.DocumentStatus;
import com.knowledgeSupport.api.domain.model.enums.DocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepositoryPort repository;

    @Mock
    private DocumentTextExtractorPort extractor;

    private DocumentService service() {
        return new DocumentService(repository, extractor);
    }

    @Test
    void upload_withExtractableText_savesIndexedDocumentWithItsChunks() {
        when(extractor.extract(any(), org.mockito.ArgumentMatchers.eq(DocumentType.PDF)))
                .thenReturn(List.of(new ExtractedPage(1, "texto da pagina um")));
        when(repository.save(any(), anyList())).thenAnswer(inv -> inv.getArgument(0));

        IndexedDocument result = service().upload("manual.pdf", "conteudo".getBytes());

        ArgumentCaptor<IndexedDocument> metaCaptor = ArgumentCaptor.forClass(IndexedDocument.class);
        ArgumentCaptor<List<DocumentChunk>> chunksCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).save(metaCaptor.capture(), chunksCaptor.capture());

        assertEquals(DocumentStatus.INDEXED, metaCaptor.getValue().getStatus());
        assertEquals("manual.pdf", metaCaptor.getValue().getName());
        assertEquals(DocumentType.PDF, metaCaptor.getValue().getType());
        assertEquals(1, chunksCaptor.getValue().size());
        assertEquals("texto da pagina um", chunksCaptor.getValue().get(0).text());
        assertEquals(1, metaCaptor.getValue().getTotalChunks());
        assertEquals(DocumentStatus.INDEXED, result.getStatus());
    }

    @Test
    void upload_whenExtractionFails_savesFailedDocumentInsteadOfThrowing() {
        when(extractor.extract(any(), org.mockito.ArgumentMatchers.eq(DocumentType.PDF)))
                .thenThrow(new DocumentExtractionException("PDF corrompido", new RuntimeException()));
        when(repository.save(any(), anyList())).thenAnswer(inv -> inv.getArgument(0));

        IndexedDocument result = service().upload("manual.pdf", "lixo".getBytes());

        assertEquals(DocumentStatus.FAILED, result.getStatus());
        assertEquals("PDF corrompido", result.getError());
        assertEquals(0, result.getTotalChunks());
        verify(repository).save(any(), org.mockito.ArgumentMatchers.eq(List.of()));
    }

    @Test
    void upload_withUnsupportedExtension_throwsWithoutCallingExtractorOrRepository() {
        assertThrows(UnsupportedDocumentTypeException.class,
                () -> service().upload("relatorio.txt", "x".getBytes()));

        verify(extractor, never()).extract(any(), any());
        verify(repository, never()).save(any(), anyList());
    }

    @Test
    void upload_scanedPdfWithNoText_isIndexedWithZeroChunks_notFailed() {
        when(extractor.extract(any(), org.mockito.ArgumentMatchers.eq(DocumentType.PDF)))
                .thenReturn(List.of(new ExtractedPage(1, "")));
        when(repository.save(any(), anyList())).thenAnswer(inv -> inv.getArgument(0));

        IndexedDocument result = service().upload("scaneado.pdf", "conteudo".getBytes());

        assertEquals(DocumentStatus.INDEXED, result.getStatus());
        assertEquals(0, result.getTotalChunks());
    }

    @Test
    void listAll_delegatesToRepository() {
        when(repository.findAll()).thenReturn(List.of(IndexedDocument.builder().build()));

        assertEquals(1, service().listAll().size());
    }

    @Test
    void deleteById_whenDocumentExists_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service().deleteById(id);

        verify(repository).deleteById(id);
    }

    @Test
    void deleteById_whenDocumentDoesNotExist_throwsAndNeverDeletes() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> service().deleteById(id));

        verify(repository, never()).deleteById(any());
    }

    @Test
    void chunksOf_whenDocumentExists_returnsChunksFromRepository() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);
        when(repository.findChunksByDocumentId(id))
                .thenReturn(List.of(new DocumentChunk(id, "doc", 1, "texto", 0)));

        List<DocumentChunk> chunks = service().chunksOf(id);

        assertEquals(1, chunks.size());
        assertTrue(chunks.get(0).text().equals("texto"));
    }

    @Test
    void chunksOf_whenDocumentDoesNotExist_throws() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> service().chunksOf(id));
    }
}
