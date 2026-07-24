package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.application.port.out.DocumentRepositoryPort;
import com.knowledgeSupport.api.domain.model.DocumentChunk;
import com.knowledgeSupport.api.domain.model.IndexedDocument;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DocumentPersistenceAdapter implements DocumentRepositoryPort {

    private final DocumentJpaRepository documentJpaRepository;
    private final DocumentChunkJpaRepository chunkJpaRepository;

    public DocumentPersistenceAdapter(DocumentJpaRepository documentJpaRepository,
                                       DocumentChunkJpaRepository chunkJpaRepository) {
        this.documentJpaRepository = documentJpaRepository;
        this.chunkJpaRepository = chunkJpaRepository;
    }

    @Override
    @Transactional
    public IndexedDocument save(IndexedDocument document, List<DocumentChunk> chunks) {
        DocumentJpaEntity savedDocument = documentJpaRepository.save(DocumentMapper.toEntity(document));
        List<DocumentChunkJpaEntity> chunkEntities = chunks.stream()
                .map(DocumentMapper::toEntityChunk)
                .toList();
        chunkJpaRepository.saveAll(chunkEntities);
        return DocumentMapper.toDomain(savedDocument);
    }

    @Override
    public List<IndexedDocument> findAll() {
        return documentJpaRepository.findAll().stream().map(DocumentMapper::toDomain).toList();
    }

    @Override
    public Optional<IndexedDocument> findById(UUID id) {
        return documentJpaRepository.findById(id).map(DocumentMapper::toDomain);
    }

    @Override
    public List<DocumentChunk> findChunksByDocumentId(UUID id) {
        String documentName = documentJpaRepository.findById(id).map(DocumentJpaEntity::getNome).orElse(null);
        return chunkJpaRepository.findByDocumentoIdOrderByPosicaoAsc(id).stream()
                .map(entity -> DocumentMapper.toDomainChunk(entity, documentName))
                .toList();
    }

    @Override
    public List<DocumentChunk> findAllChunks() {
        Map<UUID, String> namesById = documentJpaRepository.findAll().stream()
                .collect(Collectors.toMap(DocumentJpaEntity::getId, DocumentJpaEntity::getNome));
        return chunkJpaRepository.findAll().stream()
                .map(entity -> DocumentMapper.toDomainChunk(entity, namesById.get(entity.getDocumentoId())))
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        chunkJpaRepository.deleteByDocumentoId(id);
        documentJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return documentJpaRepository.existsById(id);
    }
}
