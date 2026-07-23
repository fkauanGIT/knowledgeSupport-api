package com.knowledgeSupport.api.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.UUID;

/**
 * No JPA relationship to {@link DocumentJpaEntity} on purpose — a chunk never navigates
 * back to a lazily-loaded parent; every query either has the document id already (upload,
 * /trechos) or wants every chunk regardless of parent (the search corpus). A plain FK
 * column keeps that simple instead of fighting Hibernate's lazy proxies.
 */
@Entity
public class DocumentChunkJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID documentoId;

    private Integer pagina;

    @Column(columnDefinition = "text")
    private String texto;

    private int posicao;

    protected DocumentChunkJpaEntity() {}

    public DocumentChunkJpaEntity(UUID documentoId, Integer pagina, String texto, int posicao) {
        this.documentoId = documentoId;
        this.pagina = pagina;
        this.texto = texto;
        this.posicao = posicao;
    }

    public Long getId() {
        return id;
    }

    public UUID getDocumentoId() {
        return documentoId;
    }

    public Integer getPagina() {
        return pagina;
    }

    public String getTexto() {
        return texto;
    }

    public int getPosicao() {
        return posicao;
    }
}
