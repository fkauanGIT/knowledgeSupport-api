package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.CalledAnalysis;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado da análise automática de um chamado")
public record CalledAnalysisResponse(

        @Schema(description = "Título do chamado")
        String titleCalled,

        @Schema(description = "Número da rotina do chamado")
        Integer routineNumber,

        @Schema(description = "Solução encontrada no Standard correspondente, se houver", nullable = true)
        String solution,

        @Schema(description = "Como a solução foi encontrada", example = "ROUTINE_AND_TEXT_SCORE")
        String method,

        @Schema(description = "Confiança do match, de 0 (nenhuma) a 1 (match exato)", example = "0.82")
        double score) {

    public static CalledAnalysisResponse from(CalledAnalysis analysis) {
        return new CalledAnalysisResponse(
                analysis.getCalled().getTitleCalled(),
                analysis.getCalled().getRoutineNumber(),
                analysis.getMatchedStandard() == null ? null : analysis.getMatchedStandard().getResult(),
                analysis.getMethod().getName(),
                analysis.getMethod().getScore()
        );
    }
}