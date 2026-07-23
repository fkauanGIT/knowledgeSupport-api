package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.IndexedDocument;
import com.knowledgeSupport.api.domain.model.enums.DocumentStatus;
import com.knowledgeSupport.api.domain.model.enums.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Schema(description = "Metadata of an indexed document — the chunk texts themselves are in GET /{id}/trechos")
public record DocumentMetaResponse(

        UUID id,

        @Schema(example = "Manual WinThor - Modulo Financeiro.pdf")
        String nome,

        @Schema(example = "pdf", allowableValues = {"pdf", "docx"})
        String tipo,

        int totalTrechos,

        @Schema(example = "indexado", allowableValues = {"indexado", "falhou"})
        String status,

        @Schema(description = "Parse error message, present only when status is 'falhou'", nullable = true)
        String erro,

        Instant indexadoEm) {

    public static DocumentMetaResponse from(IndexedDocument document) {
        return new DocumentMetaResponse(
                document.getId(),
                document.getName(),
                document.getType().name().toLowerCase(Locale.ROOT),
                document.getTotalChunks(),
                document.getStatus() == DocumentStatus.INDEXED ? "indexado" : "falhou",
                document.getError(),
                document.getIndexedAt());
    }
}
