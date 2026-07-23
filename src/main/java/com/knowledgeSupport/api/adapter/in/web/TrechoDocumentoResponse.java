package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.DocumentChunk;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One extracted chunk, in extraction order — for manually checking what the indexing actually captured")
public record TrechoDocumentoResponse(

        @Schema(description = "1-based page number; null for DOCX (no real pagination in the file)", nullable = true)
        Integer pagina,

        String texto) {

    public static TrechoDocumentoResponse from(DocumentChunk chunk) {
        return new TrechoDocumentoResponse(chunk.page(), chunk.text());
    }
}
