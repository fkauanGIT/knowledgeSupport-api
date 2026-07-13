package com.knowledgeSupport.api.adapter.out.jira;

import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Cobre a extração de texto da árvore ADF e a limpeza de timestamp do errorName —
 * são as duas fronteiras onde o formato bruto do Jira "morre" antes de virar Called.
 */
class JiraCalledMapperTest {

    @Test
    void extractText_concatenaTextoDeParagrafosAninhados() {
        JiraDoc texto = new JiraDoc("text", "Ausencia de troco", null);
        JiraDoc paragrafo = new JiraDoc("paragraph", null, List.of(texto));
        JiraDoc doc = new JiraDoc("doc", null, List.of(paragrafo));

        assertEquals("Ausencia de troco", JiraCalledMapper.extractText(doc));
    }

    @Test
    void extractText_comNoNulo_retornaNull() {
        assertNull(JiraCalledMapper.extractText(null));
    }

    @Test
    void stripTimestampPrefix_removeDataHoraDeRespostaDeRejeicao() {
        String bruto = "11/07/2026 11:29:52 - Resposta da Sefaz: 866 - Rejeicao: Ausencia de troco";

        assertEquals("Resposta da Sefaz: 866 - Rejeicao: Ausencia de troco",
                JiraCalledMapper.stripTimestampPrefix(bruto));
    }

    @Test
    void stripTimestampPrefix_semTimestamp_mantemTextoIntacto() {
        assertEquals("Erro generico do sistema", JiraCalledMapper.stripTimestampPrefix("Erro generico do sistema"));
    }

    @Test
    void stripTimestampPrefix_comStringNula_retornaNull() {
        assertNull(JiraCalledMapper.stripTimestampPrefix(null));
    }

    @Test
    void toDomain_mapeiaCamposEstruturadosEDerivaIncidentTypeErro() {
        JiraIssuePayload issue = new JiraIssuePayload("SUP-1115", new JiraFields(
                "Nota fiscal pendente",
                new JiraDoc("doc", null, List.of(new JiraDoc("paragraph", null, List.of(new JiraDoc("text", "descricao aqui", null))))),
                new JiraStatus("Aguardando cliente"),
                new JiraIssueType("Incidente ou Interrupções"),
                new JiraReporter("Fulano", "fulano@teste.com"),
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
    }

    @Test
    void toDomain_semReporterNemErrorName_naoQuebra() {
        JiraIssuePayload issue = new JiraIssuePayload("SUP-2", new JiraFields(
                "titulo", null, null, null, null, null, null, null, null, null
        ));

        Called called = JiraCalledMapper.toDomain(issue);

        assertNull(called.getRequester());
        assertNull(called.getErrorName());
        assertNull(called.getRoutineNumber());
        assertEquals(IncidentType.ERROR, called.getIncidentType());
    }
}
