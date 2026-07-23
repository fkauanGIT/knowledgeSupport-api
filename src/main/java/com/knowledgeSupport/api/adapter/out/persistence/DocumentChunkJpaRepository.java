package com.knowledgeSupport.api.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkJpaRepository extends JpaRepository<DocumentChunkJpaEntity, Long> {
    List<DocumentChunkJpaEntity> findByDocumentoIdOrderByPosicaoAsc(UUID documentoId);

    void deleteByDocumentoId(UUID documentoId);
}
