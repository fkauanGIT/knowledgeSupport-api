package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.application.port.out.StandardRepositoryPort;
import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledAnalysis;
import com.knowledgeSupport.api.domain.model.Standard;
import com.knowledgeSupport.api.domain.model.enums.Confidence;
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
 * Covers the AnalyzeCalledService.analyze() cascade: 1) exact match (routine + normalized
 * errorName), 2) text similarity score prioritizing candidates from the same routine,
 * 3) text score without requiring a routine, 4) NONE when nothing clears the configured
 * threshold (0.4 in the tests, same as application.yaml's default).
 * The ports (Jira and database) are mocked: no real Jira or real database needed.
 */
@ExtendWith(MockitoExtension.class)
class AnalyzeCalledServiceTest {

    @Mock
    private CalledProviderPort calledProviderPort;

    @Mock
    private StandardRepositoryPort standardRepositoryPort;

    private static final String JIRA_KEY = "SUP-1";
    private static final double THRESHOLD = 0.4;
    private static final double HIGH_CONFIDENCE_THRESHOLD = 0.75;

    private AnalyzeCalledService service() {
        return new AnalyzeCalledService(calledProviderPort, standardRepositoryPort, THRESHOLD, HIGH_CONFIDENCE_THRESHOLD);
    }

    private Called calledWith(Integer routineNumber, String errorName) {
        return calledWithFreeText(routineNumber, "test", "description", errorName);
    }

    private Called calledWithFreeText(Integer routineNumber, String title, String description, String errorName) {
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
        return standardWithFreeText(standardName, "standard text", routineNumber, result);
    }

    private Standard standardWithFreeText(String standardName, String text, Integer routineNumber, String result) {
        return Standard.builder()
                .standardName(standardName)
                .text(text)
                .result(result)
                .incidentType(IncidentType.ERROR)
                .routineNumber(routineNumber)
                .build();
    }

    @Test
    void findsByExactMatch_whenRoutineAndErrorMatchAndThereIsAResult() {
        Called called = calledWith(1301, "Routine 1301 stuck");
        Standard standard = standardWith("Routine 1301 stuck", 1301, "Run the manual reprocessing from screen X");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("ROUTINE_AND_ERROR_NAME", analysis.getMethod().getName());
        assertEquals(1.0, analysis.getMethod().getScore());
        assertEquals(Confidence.CONFIRMED, analysis.getMethod().getConfidence());
        assertNotNull(analysis.getMatchedStandard());
        assertEquals("Run the manual reprocessing from screen X", analysis.getMatchedStandard().getResult());
    }

    @Test
    void ignoresCaseAccentsAndSpacesInExactMatch() {
        Called called = calledWith(1301, "  caixa NÃO fecha  ");
        Standard standard = standardWith("Caixa nao Fecha", 1301, "Solução X");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("ROUTINE_AND_ERROR_NAME", analysis.getMethod().getName());
        assertEquals(Confidence.CONFIRMED, analysis.getMethod().getConfidence());
    }

    @Test
    void returnsNoneWhenRoutineAndErrorMatchButStandardHasNoSolution() {
        Called called = calledWith(1301, "Routine 1301 stuck");
        Standard standard = standardWith("Routine 1301 stuck", 1301, null);

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("NONE", analysis.getMethod().getName());
        assertEquals(Confidence.NONE, analysis.getMethod().getConfidence());
        assertNull(analysis.getMatchedStandard());
    }

    @Test
    void returnsNoneWhenRoutineAndErrorMatchButResultIsBlank() {
        Called called = calledWith(1301, "Routine 1301 stuck");
        Standard standard = standardWith("Routine 1301 stuck", 1301, "   ");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("NONE", analysis.getMethod().getName());
        assertEquals(Confidence.NONE, analysis.getMethod().getConfidence());
        assertNull(analysis.getMatchedStandard());
    }

    @Test
    void returnsNoneWhenRoutineMatchesButTextHasNothingInCommon() {
        Called called = calledWith(1301, "Different error than the registered one");
        Standard standard = standardWith("Routine 1301 stuck", 1301, "Solução X");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("NONE", analysis.getMethod().getName());
        assertEquals(Confidence.NONE, analysis.getMethod().getConfidence());
        assertNull(analysis.getMatchedStandard());
    }

    @Test
    void findsByTextScore_whenRoutineMatchesButErrorNameDoesNotMatchExactly() {
        Called called = calledWithFreeText(1301,
                "Caixa trava no encerramento",
                "operador nao consegue encerrar caixa",
                "Bloqueio generico");
        Standard standard = standardWithFreeText(
                "Bloqueio ao encerrar caixa",
                "operador nao consegue encerrar caixa apos movimento",
                1301,
                "Verificar tabela de fechamento de caixa");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("ROUTINE_AND_TEXT_SCORE", analysis.getMethod().getName());
        assertTrue(analysis.getMethod().getScore() >= THRESHOLD, "score should clear the threshold: " + analysis.getMethod().getScore());
        // Containment score here is 6/9 ~= 0.667: clears the minimum threshold but stays below
        // HIGH_CONFIDENCE_THRESHOLD (0.75) -> UNCERTAIN, not a rubber stamp.
        assertEquals(Confidence.UNCERTAIN, analysis.getMethod().getConfidence());
        assertNotNull(analysis.getMatchedStandard());
    }

    @Test
    void findsByTextScore_evenWhenRoutineDoesNotMatch() {
        Called called = calledWithFreeText(9999,
                "Caixa trava no encerramento",
                "operador nao consegue encerrar caixa",
                "Bloqueio generico");
        Standard standard = standardWithFreeText(
                "Bloqueio ao encerrar caixa",
                "operador nao consegue encerrar caixa apos movimento",
                1301,
                "Verificar tabela de fechamento de caixa");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        assertEquals("TEXT_SCORE", analysis.getMethod().getName());
        assertTrue(analysis.getMethod().getScore() >= THRESHOLD, "score should clear the threshold: " + analysis.getMethod().getScore());
        assertEquals(Confidence.UNCERTAIN, analysis.getMethod().getConfidence());
        assertNotNull(analysis.getMatchedStandard());
    }

    @Test
    void findsByScore_evenWithATypoInErrorName() {
        Called called = calledWithFreeText(1301, null, null, "Fechamentoo duplicado novamente");
        Standard standard = standardWithFreeText("Fechamento duplicado", "ocorre novamente as vezes", 1301, "Cancelar o duplicado");

        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.of(called));
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard));

        CalledAnalysis analysis = service().analyze(JIRA_KEY);

        // The typo makes the exact match (1.1) fail; the score's typo tolerance (1.3) resolves it.
        // Full containment (3/3 tokens) despite the typo -> LIKELY, not just "cleared the threshold".
        assertEquals("ROUTINE_AND_TEXT_SCORE", analysis.getMethod().getName());
        assertEquals(Confidence.LIKELY, analysis.getMethod().getConfidence());
        assertNotNull(analysis.getMatchedStandard());
    }

    @Test
    void throwsExceptionWhenTicketDoesNotExistInJira() {
        when(calledProviderPort.fetchByKey(JIRA_KEY)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service().analyze(JIRA_KEY));
    }
}
