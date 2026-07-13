package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.RoutineGap;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Quanto uma rotina pesa nas lacunas de cadastro")
public record RoutineGapResponse(

        @Schema(description = "Número da rotina (null = chamados sem rotina preenchida)", nullable = true)
        Integer routineNumber,

        @Schema(description = "Quantos chamados dessa rotina não encontraram Standard")
        int quantidade,

        @Schema(description = "Percentual do total de lacunas que essa rotina sozinha representa")
        double percentualDasLacunas,

        @Schema(description = "Títulos de exemplo, pra saber o que cadastrar")
        List<String> exemplos) {

    public static RoutineGapResponse from(RoutineGap gap) {
        return new RoutineGapResponse(gap.getRoutineNumber(), gap.getQuantidade(), gap.getPercentualDasLacunas(), gap.getExemplos());
    }
}
