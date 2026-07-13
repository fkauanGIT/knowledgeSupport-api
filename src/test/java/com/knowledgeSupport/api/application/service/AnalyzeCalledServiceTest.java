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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Cobre a cascata do AnalyzeCalledService.analyze(): 1) match exato (rotina + errorName
 * normalizado), 2) score de similaridade de texto priorizando candidatos da mesma rotina,
 * 3) score de texto sem exigir rotina, 4) NONE quando nada passa do threshold configurado
 * (0.4 nos testes, mesmo valor default do application.yaml).
 * As portas (Jira e banco) são mockadas: não é preciso Jira real nem banco real.
 */
@ExtendWith(MockitoExtension.class)
class AnalyzeCalledServiceTest {

    @Mock
    private CalledProviderPort calledProviderPort;

    @Mock
    private StandardRepositoryPort standardRepositoryPort;

    private static final String JIRA_KEY = "SUP-1";
    private static final double THRESHOLD = 0.4;

    private AnalyzeCalledService service() {
        return new AnalyzeCalledService(calledProviderPort, standardRepositoryPort, THRESHOLD);
    }

    private Called calledWith(Integer routineNumber, String errorName) {
        return calledComTextoLivre(routineNumber, "teste", "descricao", errorName);
    }

    private Called calledComTextoLivre(Integer routineNumber, String title, String description, String errorName) {
        return Called.builder()
                .titleCalled(title)
                .descriptionCalled(description)
                .errorName(errorName)
                .incidentType(IncidentType.ERROR)
                .filterCategory(FilterCategory.PENDING)
                .routineNumber(routineNumber)
                .build();
    }

    private Standard standardWith(String standardName, Integer routineNumber, String result) {
        return standardComTextoLivre(standardName, "texto do padrao", routineNumber, result);
    }

    private Standard standardComTextoLivre(String standardName, String text, Integer routineNumber, String result) {
        return Standard.builder()
                .standardName(standardName)
                .text(text)
                .result(result)
                .incidentType(IncidentType.ERROR)
                .routineNumber(routineNumber)
                .build();
    }

    @Test
    void deveEncontrarPorMatchExato_QuandoRotinaEErroBatemEHaResult() {
        Called called = calledWith(1301, "Rotina 1301 travando");
        Standard standard = standardWith("Rotina 1301 travando", 1301, "Rodar o reprocessamento manual pela tela X");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("ROUTINE_AND_ERROR_NAME", analysis.getMethod().getName());
        assertEquals(1.0, analysis.getMethod().getScore());
        assertNotNull(analysis.getMatchedStandard());
        assertEquals("Rodar o reprocessamento manual pela tela X", analysis.getMatchedStandard().getResult());
    }

    @Test
    void deveIgnorarCaixaAcentoEEspacosNoMatchExato() {
        Called called = calledWith(1301, "  caixa NÃO fecha  ");
        Standard standard = standardWith("Caixa nao Fecha", 1301, "Solução X");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("ROUTINE_AND_ERROR_NAME", analysis.getMethod().getName());
    }

    @Test
    void deveRetornarNoneQuandoRotinaEErroBatemMasStandardNaoTemSolucao() {
        Called called = calledWith(1301, "Rotina 1301 travando");
        Standard standard = standardWith("Rotina 1301 travando", 1301, null);

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("NONE", analysis.getMethod().getName());
        assertNull(analysis.getMatchedStandard());
    }

    @Test
    void deveRetornarNoneQuandoRotinaEErroBatemMasResultEBranco() {
        Called called = calledWith(1301, "Rotina 1301 travando");
        Standard standard = standardWith("Rotina 1301 travando", 1301, "   ");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("NONE", analysis.getMethod().getName());
        assertNull(analysis.getMatchedStandard());
    }

    @Test
    void deveRetornarNoneQuandoRotinaBateMasTextoNaoTemNadaEmComum() {
        Called called = calledWith(1301, "Erro diferente do cadastrado");
        Standard standard = standardWith("Rotina 1301 travando", 1301, "Solução X");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("NONE", analysis.getMethod().getName());
        assertNull(analysis.getMatchedStandard());
    }

    @Test
    void deveEncontrarPorScoreDeTexto_QuandoRotinaBateMasErrorNameNaoBateExatamente() {
        Called called = calledComTextoLivre(1301,
                "Caixa trava no encerramento",
                "operador nao consegue encerrar caixa",
                "Bloqueio generico");
        Standard standard = standardComTextoLivre(
                "Bloqueio ao encerrar caixa",
                "operador nao consegue encerrar caixa apos movimento",
                1301,
                "Verificar tabela de fechamento de caixa");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("ROUTINE_AND_TEXT_SCORE", analysis.getMethod().getName());
        assertTrue(analysis.getMethod().getScore() >= THRESHOLD, "score deveria passar do threshold: " + analysis.getMethod().getScore());
        assertNotNull(analysis.getMatchedStandard());
    }

    @Test
    void deveEncontrarPorScoreDeTexto_MesmoQuandoRotinaNaoBate() {
        Called called = calledComTextoLivre(9999,
                "Caixa trava no encerramento",
                "operador nao consegue encerrar caixa",
                "Bloqueio generico");
        Standard standard = standardComTextoLivre(
                "Bloqueio ao encerrar caixa",
                "operador nao consegue encerrar caixa apos movimento",
                1301,
                "Verificar tabela de fechamento de caixa");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("TEXT_SCORE", analysis.getMethod().getName());
        assertTrue(analysis.getMethod().getScore() >= THRESHOLD, "score deveria passar do threshold: " + analysis.getMethod().getScore());
        assertNotNull(analysis.getMatchedStandard());
    }

    @Test
    void deveEncontrarPorScore_MesmoComTypoNoErrorName() {
        Called called = calledComTextoLivre(1301, null, null, "Fechamentoo duplicado novamente");
        Standard standard = standardComTextoLivre("Fechamento duplicado", "ocorre novamente as vezes", 1301, "Cancelar o duplicado");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        // O typo faz o match exato (1.1) falhar; quem resolve é a tolerância a typo do score (1.3).
        assertEquals("ROUTINE_AND_TEXT_SCORE", analysis.getMethod().getName());
        assertNotNull(analysis.getMatchedStandard());
    }

    @Test
    void deveLancarExcecaoQuandoChamadoNaoExisteNoJira() {
        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service().analyze(JIRA_KEY));
    }
}
