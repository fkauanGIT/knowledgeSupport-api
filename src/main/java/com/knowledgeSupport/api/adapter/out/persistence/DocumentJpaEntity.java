package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.domain.model.enums.DocumentStatus;
import com.knowledgeSupport.api.domain.model.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.UUID;

/**
 * Id is assigned by the application (UUID.randomUUID()), not DB-generated: the service needs
 * the document's id up front to stamp it onto every {@link DocumentChunkJpaEntity} row it
 * builds in the same request, before the document itself is ever persisted.
 */
@Entity
public class DocumentJpaEntity {
    @Id
    private UUID id;

    private String nome;

    @Enumerated(EnumType.STRING)
    private DocumentType tipo;

    private int totalTrechos;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Column(columnDefinition = "text")
    private String erro;

    private Instant indexadoEm;

    protected DocumentJpaEntity() {}

    public DocumentJpaEntity(UUID id, String nome, DocumentType tipo, int totalTrechos, DocumentStatus status,
                              String erro, Instant indexadoEm) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.totalTrechos = totalTrechos;
        this.status = status;
        this.erro = erro;
        this.indexadoEm = indexadoEm;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public DocumentType getTipo() {
        return tipo;
    }

    public int getTotalTrechos() {
        return totalTrechos;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public String getErro() {
        return erro;
    }

    public Instant getIndexadoEm() {
        return indexadoEm;
    }
}
