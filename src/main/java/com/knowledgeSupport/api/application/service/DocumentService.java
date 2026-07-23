package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.in.DeleteDocumentUseCase;
import com.knowledgeSupport.api.application.port.in.GetDocumentChunksUseCase;
import com.knowledgeSupport.api.application.port.in.ListDocumentsUseCase;
import com.knowledgeSupport.api.application.port.in.UploadDocumentUseCase;
import com.knowledgeSupport.api.application.port.out.DocumentExtractionException;
import com.knowledgeSupport.api.application.port.out.DocumentRepositoryPort;
import com.knowledgeSupport.api.application.port.out.DocumentTextExtractorPort;
import com.knowledgeSupport.api.application.port.out.ExtractedPage;
import com.knowledgeSupport.api.domain.model.DocumentChunk;
import com.knowledgeSupport.api.domain.model.IndexedDocument;
import com.knowledgeSupport.api.domain.model.enums.DocumentStatus;
import com.knowledgeSupport.api.domain.model.enums.DocumentType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Core: upload/list/delete/read-chunks for indexed documents. Extraction (PDF/DOCX parsing)
 * and persistence are both outbound ports — this class only orchestrates them and decides
 * what counts as success vs. failure.
 */
@Service
public class DocumentService implements UploadDocumentUseCase, ListDocumentsUseCase, DeleteDocumentUseCase,
        GetDocumentChunksUseCase {

    private final DocumentRepositoryPort repository;
    private final DocumentTextExtractorPort extractor;

    public DocumentService(DocumentRepositoryPort repository, DocumentTextExtractorPort extractor) {
        this.repository = repository;
        this.extractor = extractor;
    }

    @Override
    public IndexedDocument upload(String filename, byte[] content) {
        DocumentType type = resolveType(filename);
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        try {
            List<ExtractedPage> pages = extractor.extract(content, type);
            List<DocumentChunk> chunks = buildChunks(id, filename, pages);
            IndexedDocument meta = IndexedDocument.builder()
                    .id(id)
                    .name(filename)
                    .type(type)
                    .totalChunks(chunks.size())
                    .status(DocumentStatus.INDEXED)
                    .indexedAt(now)
                    .build();
            return repository.save(meta, chunks);
        } catch (DocumentExtractionException e) {
            IndexedDocument meta = IndexedDocument.builder()
                    .id(id)
                    .name(filename)
                    .type(type)
                    .totalChunks(0)
                    .status(DocumentStatus.FAILED)
                    .error(e.getMessage())
                    .indexedAt(now)
                    .build();
            return repository.save(meta, List.of());
        }
    }

    @Override
    public List<IndexedDocument> listAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        requireExists(id);
        repository.deleteById(id);
    }

    @Override
    public List<DocumentChunk> chunksOf(UUID documentId) {
        requireExists(documentId);
        return repository.findChunksByDocumentId(documentId);
    }

    private void requireExists(UUID id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Documento não encontrado: " + id);
        }
    }

    private List<DocumentChunk> buildChunks(UUID documentId, String documentName, List<ExtractedPage> pages) {
        List<DocumentChunk> chunks = new ArrayList<>();
        int position = 0;
        for (ExtractedPage page : pages) {
            for (String piece : TextChunker.chunk(page.text())) {
                chunks.add(new DocumentChunk(documentId, documentName, page.page(), piece, position++));
            }
        }
        return chunks;
    }

    private DocumentType resolveType(String filename) {
        String lower = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return DocumentType.PDF;
        if (lower.endsWith(".docx")) return DocumentType.DOCX;
        throw new UnsupportedDocumentTypeException(
                "Tipo de arquivo não suportado (use PDF ou DOCX): " + filename);
    }
}
