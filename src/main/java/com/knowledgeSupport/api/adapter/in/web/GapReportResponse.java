package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.GapReport;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Onde cadastrar um Standard novo cobre mais chamados sem solução sugerida")
public record GapReportResponse(

        @Schema(description = "Total de chamados abertos analisados")
        int totalChamadosAnalisados,

        @Schema(description = "Quantos desses não encontraram Standard (method NONE)")
        int totalSemMatch,

        @Schema(description = "Lacunas agrupadas por rotina, da maior pra menor")
        List<RoutineGapResponse> lacunasPorRotina) {

    public static GapReportResponse from(GapReport report) {
        return new GapReportResponse(
                report.getTotalChamadosAnalisados(),
                report.getTotalSemMatch(),
                report.getLacunasPorRotina().stream().map(RoutineGapResponse::from).toList()
        );
    }
}
