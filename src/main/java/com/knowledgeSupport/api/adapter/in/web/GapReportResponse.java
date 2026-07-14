package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.GapReport;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Where registering a new Standard covers more tickets with no suggested solution")
public record GapReportResponse(

        @Schema(description = "Total number of open tickets analyzed")
        int totalCalledsAnalyzed,

        @Schema(description = "How many of those found no Standard (method NONE)")
        int totalWithoutMatch,

        @Schema(description = "Gaps grouped by routine, from largest to smallest")
        List<RoutineGapResponse> gapsByRoutine) {

    public static GapReportResponse from(GapReport report) {
        return new GapReportResponse(
                report.getTotalCalledsAnalyzed(),
                report.getTotalWithoutMatch(),
                report.getGapsByRoutine().stream().map(RoutineGapResponse::from).toList()
        );
    }
}
