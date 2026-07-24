package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.application.port.out.StandardRepositoryPort;
import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledFilter;
import com.knowledgeSupport.api.domain.model.GapReport;
import com.knowledgeSupport.api.domain.model.RoutineGap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * With no Standard registered, every ticket becomes a gap — that keeps the test
 * deterministic without needing to predict a text score, only the grouping by routine.
 */
@ExtendWith(MockitoExtension.class)
class GapReportServiceTest {

    @Mock
    private CalledProviderPort calledProviderPort;

    @Mock
    private StandardRepositoryPort standardRepositoryPort;

    private GapReportService service() {
        return new GapReportService(calledProviderPort, standardRepositoryPort, 0.4, 0.75);
    }

    private Called calledFromRoutine(Integer routineNumber, String title) {
        return Called.builder()
                .titleCalled(title)
                .routineNumber(routineNumber)
                .build();
    }

    @Test
    void groupsByRoutineAndSortsByCountDesc() {
        when(calledProviderPort.fetchOpenCalleds(CalledFilter.NONE)).thenReturn(List.of(
                calledFromRoutine(100, "ticket 1"),
                calledFromRoutine(100, "ticket 2"),
                calledFromRoutine(200, "ticket 3"),
                calledFromRoutine(null, "ticket 4")
        ));
        when(standardRepositoryPort.findAll()).thenReturn(List.of());

        GapReport report = service().generate();

        assertEquals(4, report.getTotalCalledsAnalyzed());
        assertEquals(4, report.getTotalWithoutMatch());
        assertEquals(3, report.getGapsByRoutine().size());

        RoutineGap first = report.getGapsByRoutine().get(0);
        assertEquals(100, first.getRoutineNumber());
        assertEquals(2, first.getCount());
        assertEquals(50.0, first.getPercentageOfGaps());
        assertTrue(first.getExamples().contains("ticket 1"));
    }

    @Test
    void noOpenTickets_returnsEmptyReport() {
        when(calledProviderPort.fetchOpenCalleds(CalledFilter.NONE)).thenReturn(List.of());

        GapReport report = service().generate();

        assertEquals(0, report.getTotalCalledsAnalyzed());
        assertEquals(0, report.getTotalWithoutMatch());
        assertTrue(report.getGapsByRoutine().isEmpty());
    }

    @Test
    void ticketWithExactMatch_isNotIncludedInTheReport() {
        Called called = Called.builder()
                .titleCalled("test")
                .errorName("Routine 100 stuck")
                .routineNumber(100)
                .build();

        com.knowledgeSupport.api.domain.model.Standard standard = com.knowledgeSupport.api.domain.model.Standard.builder()
                .standardName("Routine 100 stuck")
                .routineNumber(100)
                .result("Solution")
                .build();

        when(calledProviderPort.fetchOpenCalleds(CalledFilter.NONE)).thenReturn(List.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        GapReport report = service().generate();

        assertEquals(1, report.getTotalCalledsAnalyzed());
        assertEquals(0, report.getTotalWithoutMatch());
        assertTrue(report.getGapsByRoutine().isEmpty());
    }
}
