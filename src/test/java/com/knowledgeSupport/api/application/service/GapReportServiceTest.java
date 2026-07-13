package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.application.port.out.StandardRepositoryPort;
import com.knowledgeSupport.api.domain.model.Called;
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
 * Sem nenhum Standard cadastrado, todo chamado vira lacuna — isso deixa o teste
 * determinístico sem precisar prever score de texto, só a agregação por rotina.
 */
@ExtendWith(MockitoExtension.class)
class GapReportServiceTest {

    @Mock
    private CalledProviderPort calledProviderPort;

    @Mock
    private StandardRepositoryPort standardRepositoryPort;

    private GapReportService service() {
        return new GapReportService(calledProviderPort, standardRepositoryPort, 0.4);
    }

    private Called calledDaRotina(Integer routineNumber, String titulo) {
        return Called.builder()
                .titleCalled(titulo)
                .routineNumber(routineNumber)
                .build();
    }

    @Test
    void agrupaPorRotinaEOrdenaPorQuantidadeDesc() {
        when(calledProviderPort.fetchOpenCalleds()).thenReturn(List.of(
                calledDaRotina(100, "chamado 1"),
                calledDaRotina(100, "chamado 2"),
                calledDaRotina(200, "chamado 3"),
                calledDaRotina(null, "chamado 4")
        ));
        when(standardRepositoryPort.findAll()).thenReturn(List.of());

        GapReport report = service().generate();

        assertEquals(4, report.getTotalChamadosAnalisados());
        assertEquals(4, report.getTotalSemMatch());
        assertEquals(3, report.getLacunasPorRotina().size());

        RoutineGap primeira = report.getLacunasPorRotina().get(0);
        assertEquals(100, primeira.getRoutineNumber());
        assertEquals(2, primeira.getQuantidade());
        assertEquals(50.0, primeira.getPercentualDasLacunas());
        assertTrue(primeira.getExemplos().contains("chamado 1"));
    }

    @Test
    void semChamadosAbertos_devolveRelatorioVazio() {
        when(calledProviderPort.fetchOpenCalleds()).thenReturn(List.of());

        GapReport report = service().generate();

        assertEquals(0, report.getTotalChamadosAnalisados());
        assertEquals(0, report.getTotalSemMatch());
        assertTrue(report.getLacunasPorRotina().isEmpty());
    }

    @Test
    void chamadoComMatchExatoNaoEntraNoRelatorio() {
        Called called = Called.builder()
                .titleCalled("teste")
                .errorName("Rotina 100 travando")
                .routineNumber(100)
                .build();

        com.knowledgeSupport.api.domain.model.Standard standard = com.knowledgeSupport.api.domain.model.Standard.builder()
                .standardName("Rotina 100 travando")
                .routineNumber(100)
                .result("Solução")
                .build();

        when(calledProviderPort.fetchOpenCalleds()).thenReturn(List.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        GapReport report = service().generate();

        assertEquals(1, report.getTotalChamadosAnalisados());
        assertEquals(0, report.getTotalSemMatch());
        assertTrue(report.getLacunasPorRotina().isEmpty());
    }
}
