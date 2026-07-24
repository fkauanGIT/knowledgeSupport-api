package com.knowledgeSupport.api.adapter.out.jira;

import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Covers extracting text from the ADF tree and stripping the timestamp from errorName —
 * the two boundaries where Jira's raw format "dies" before becoming a Called.
 */
class JiraCalledMapperTest {

    @Test
    void extractText_concatenatesTextFromNestedParagraphs() {
        JiraDoc text = new JiraDoc("text", "Ausencia de troco", null);
        JiraDoc paragraph = new JiraDoc("paragraph", null, List.of(text));
        JiraDoc doc = new JiraDoc("doc", null, List.of(paragraph));

        assertEquals("Ausencia de troco", JiraCalledMapper.extractText(doc));
    }

    @Test
    void extractText_withNullNode_returnsNull() {
        assertNull(JiraCalledMapper.extractText(null));
    }

    @Test
    void stripTimestampPrefix_removesDateTimeFromRejectionResponse() {
        String raw = "11/07/2026 11:29:52 - Resposta da Sefaz: 866 - Rejeicao: Ausencia de troco";

        assertEquals("Resposta da Sefaz: 866 - Rejeicao: Ausencia de troco",
                JiraCalledMapper.stripTimestampPrefix(raw));
    }

    @Test
    void stripTimestampPrefix_withNoTimestamp_keepsTextIntact() {
        assertEquals("Erro generico do sistema", JiraCalledMapper.stripTimestampPrefix("Erro generico do sistema"));
    }

    @Test
    void stripTimestampPrefix_withNullString_returnsNull() {
        assertNull(JiraCalledMapper.stripTimestampPrefix(null));
    }

    @Test
    void toDomain_mapsStructuredFieldsAndDerivesErrorIncidentType() {
        JiraIssuePayload issue = new JiraIssuePayload("SUP-1115", new JiraFields(
                "Nota fiscal pendente",
                new JiraDoc("doc", null, List.of(new JiraDoc("paragraph", null, List.of(new JiraDoc("text", "descricao aqui", null))))),
                new JiraStatus("Aguardando cliente"),
                new JiraIssueType("Incidente ou Interrupções"),
                new JiraReporter("Fulano", "fulano@teste.com"),
                new JiraReporter("Ciclano", "ciclano@teste.com"),
                "2026-07-11T14:55:32.988+0000",
                "2026-07-20",
                "2026-07-13T18:02:55.565+0000",
                1452.0,
                new JiraDoc("doc", null, List.of(new JiraDoc("paragraph", null, List.of(new JiraDoc("text", "11/07/2026 11:29:52 - Rejeicao 866", null)))))
        ));

        Called called = JiraCalledMapper.toDomain(issue);

        assertEquals("SUP-1115", called.getJiraKey());
        assertEquals("Nota fiscal pendente", called.getTitleCalled());
        assertEquals("descricao aqui", called.getDescriptionCalled());
        assertEquals(1452, called.getRoutineNumber());
        assertEquals("Rejeicao 866", called.getErrorName());
        assertEquals(IncidentType.ERROR, called.getIncidentType());
        assertEquals("Aguardando cliente", called.getStatus());
        assertEquals("Fulano", called.getRequester().getRequesterName());
        assertEquals("Ciclano", called.getAssigneeName());
    }

    @Test
    void toDomain_withNoReporterNorErrorName_doesNotBreak() {
        JiraIssuePayload issue = new JiraIssuePayload("SUP-2", new JiraFields(
                "titulo", null, null, null, null, null, null, null, null, null, null
        ));

        Called called = JiraCalledMapper.toDomain(issue);

        assertNull(called.getRequester());
        assertNull(called.getAssigneeName());
        assertNull(called.getErrorName());
        assertNull(called.getRoutineNumber());
        assertEquals(IncidentType.ERROR, called.getIncidentType());
    }
}
