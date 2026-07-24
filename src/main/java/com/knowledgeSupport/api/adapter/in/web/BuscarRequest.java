package com.knowledgeSupport.api.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Free-text search over every indexed document's chunks")
public record BuscarRequest(

        @Schema(description = "Usually a ticket's title + errorName, concatenated", example = "erro ao gerar NF-e rotina faturamento")
        String consulta) {
}
