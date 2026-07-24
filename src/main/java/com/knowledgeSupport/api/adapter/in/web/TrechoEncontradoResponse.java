package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.ChunkMatch;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "A chunk matched by the search, ranked by relevance")
public record TrechoEncontradoResponse(

        UUID docId,

        String docNome,

        @Schema(nullable = true)
        Integer pagina,

        String texto,

        @Schema(description = "0-100, relative to the best result of this search (best = 100)")
        int relevancia) {

    public static TrechoEncontradoResponse from(ChunkMatch match) {
        return new TrechoEncontradoResponse(match.documentId(), match.documentName(), match.page(), match.text(),
                match.relevance());
    }
}
