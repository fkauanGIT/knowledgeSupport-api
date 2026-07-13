package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.application.port.out.StandardRepositoryPort;
import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledAnalysis;
import com.knowledgeSupport.api.domain.model.Standard;
import com.knowledgeSupport.api.domain.model.enums.FilterCategory;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Cobre a lógica de comparação do AnalyzeCalledService.analyze(), que hoje
 * exige rotina E nome do erro batendo com um Standard.
 * As portas (Jira e banco) são mockadas: não é preciso Jira real nem banco real
 * para testar as bordas do "&&" entre rotina e nome do erro.
 */
@ExtendWith(MockitoExtension.class)
class AnalyzeCalledServiceTest {

    @Mock
    private CalledProviderPort calledProviderPort;

    @Mock
    private StandardRepositoryPort standardRepositoryPort;

    private AnalyzeCalledService service;

    private static final String JIRA_KEY = "SUP-1";

    private Called calledWith(Integer routineNumber, String errorName) {
        return new Called(
                "teste",
                "descricao",
                errorName,
                IncidentType.ERROR,
                FilterCategory.PENDING,
                null,
                null,
                null,
                null,
                routineNumber
        );
    }

    private Standard standardWith(String standardName, Integer routineNumber, String result) {
        return new Standard(standardName, "texto do padrao", result, IncidentType.ERROR, routineNumber);
    }

    @Test
    void deveRetornarNoneQuandoSomenteARotinaBate() {
        service = new AnalyzeCalledService(calledProviderPort, standardRepositoryPort);

        Called called = calledWith(1301, "Erro diferente do cadastrado");
        Standard standard = standardWith("Rotina 1301 travando", 1301, "Solução X");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service.analyze(JIRA_KEY);

        assertEquals("NONE", analysis.getMethod());
        assertNull(analysis.getMatchedStandard());
    }

    @Test
    void deveRetornarNoneQuandoSomenteONomeDoErroBate() {
        service = new AnalyzeCalledService(calledProviderPort, standardRepositoryPort);

        Called called = calledWith(9999, "Rotina 1301 travando");
        Standard standard = standardWith("Rotina 1301 travando", 1301, "Solução X");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service.analyze(JIRA_KEY);

        assertEquals("NONE", analysis.getMethod());
        assertNull(analysis.getMatchedStandard());
    }

    @Test
    void deveRetornarNoneQuandoRotinaEErroBatemMasStandardNaoTemSolucao() {
        service = new AnalyzeCalledService(calledProviderPort, standardRepositoryPort);

        Called called = calledWith(1301, "Rotina 1301 travando");
        Standard standard = standardWith("Rotina 1301 travando", 1301, null);

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service.analyze(JIRA_KEY);

        assertEquals("NONE", analysis.getMethod());
        assertNull(analysis.getMatchedStandard());
    }

    @Test
    void deveRetornarNoneQuandoRotinaEErroBatemMasResultEBranco() {
        service = new AnalyzeCalledService(calledProviderPort, standardRepositoryPort);

        Called called = calledWith(1301, "Rotina 1301 travando");
        Standard standard = standardWith("Rotina 1301 travando", 1301, "   ");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service.analyze(JIRA_KEY);

        assertEquals("NONE", analysis.getMethod());
        assertNull(analysis.getMatchedStandard());
    }

    @Test
    void deveEncontrarStandardComSolucao_QuandoRotinaEErroBatemEHaResult() {
        service = new AnalyzeCalledService(calledProviderPort, standardRepositoryPort);

        Called called = calledWith(1301, "Rotina 1301 travando");
        Standard standard = standardWith("Rotina 1301 travando", 1301, "Rodar o reprocessamento manual pela tela X");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service.analyze(JIRA_KEY);

        assertEquals("ROUTINE_AND_ERROR_NAME", analysis.getMethod());
        assertNotNull(analysis.getMatchedStandard());
        assertEquals("Rodar o reprocessamento manual pela tela X", analysis.getMatchedStandard().getResult());
    }

    @Test
    void deveIgnorarCaixaEEspacosAoCompararONomeDoErro() {
        service = new AnalyzeCalledService(calledProviderPort, standardRepositoryPort);

        Called called = calledWith(1301, "  rotina 1301 TRAVANDO  ");
        Standard standard = standardWith("Rotina 1301 travando", 1301, "Solução X");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service.analyze(JIRA_KEY);

        assertEquals("ROUTINE_AND_ERROR_NAME", analysis.getMethod());
    }

    @Test
    void deveLancarExcecaoQuandoChamadoNaoExisteNoJira() {
        service = new AnalyzeCalledService(calledProviderPort, standardRepositoryPort);

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.analyze(JIRA_KEY));
    }
}
