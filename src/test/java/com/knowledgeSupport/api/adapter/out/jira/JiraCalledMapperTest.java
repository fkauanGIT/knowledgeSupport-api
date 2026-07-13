package com.knowledgeSupport.api.adapter.out.jira;

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
}
