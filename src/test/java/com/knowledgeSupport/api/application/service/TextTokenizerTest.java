package com.knowledgeSupport.api.application.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextTokenizerTest {

    @Test
    void normalize_stripsAccentsAndLowercases() {
        assertEquals("nota fiscal nao emitida", TextTokenizer.normalize("Nota Fiscal NÃO Emitida"));
    }

    @Test
    void tokenize_extractsOnlyAlphanumericRuns() {
        assertEquals(List.of("erro", "866", "sefaz", "rejeicao"),
                TextTokenizer.tokenize("Erro 866: Sefaz - Rejeição!"));
    }

    @Test
    void tokenize_isAccentInsensitive() {
        assertEquals(TextTokenizer.tokenize("nao emitida"), TextTokenizer.tokenize("NÃO EMITIDA"));
    }

    @Test
    void tokenize_withNullOrBlank_returnsEmptyList() {
        assertTrue(TextTokenizer.tokenize(null).isEmpty());
        assertTrue(TextTokenizer.tokenize("   ").isEmpty());
    }
}
