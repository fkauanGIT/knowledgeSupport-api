package com.knowledgeSupport.api.application.service;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests normalize/tokenize/score in isolation, with no need to build Called/Standard/mocks.
 * AnalyzeCalledServiceTest covers the cascade's integration; this is just the text algorithm.
 */
class TextSimilarityTest {

    @Test
    void normalize_removesAccentsCaseAndDuplicateSpaces() {
        assertEquals("nao fecha direito", TextSimilarity.normalize("  Não FECHA   direito "));
    }

    @Test
    void normalize_withNullString_returnsEmpty() {
        assertEquals("", TextSimilarity.normalize(null));
    }

    @Test
    void tokenize_removesStopwordsButKeepsNegation() {
        Set<String> tokens = TextSimilarity.tokenize("O caixa não fecha de jeito nenhum!");

        assertTrue(tokens.contains("caixa"));
        assertTrue(tokens.contains("nao"), "negation must not become a stopword");
        assertTrue(tokens.contains("fecha"));
        assertTrue(tokens.contains("jeito"));
        assertTrue(tokens.contains("nenhum"));
        assertFalse(tokens.contains("o"));
        assertFalse(tokens.contains("de"));
    }

    @Test
    void score_identicalTextsIgnoringAccentAndCase_returnsOne() {
        double score = TextSimilarity.score("Caixa não fecha", "caixa NAO fecha");

        assertEquals(1.0, score);
    }

    @Test
    void score_textsWithNoWordInCommon_returnsZero() {
        double score = TextSimilarity.score("Relatorio de vendas trava", "Estoque duplicado no cadastro");

        assertEquals(0.0, score);
    }

    @Test
    void score_emptyText_returnsZero() {
        assertEquals(0.0, TextSimilarity.score("", "qualquer coisa"));
        assertEquals(0.0, TextSimilarity.score(null, "qualquer coisa"));
    }

    @Test
    void score_toleratesASingleLetterTypo() {
        // "fechamneto" has two letters swapped compared to "fechamento"
        double score = TextSimilarity.score("erro de fechamento duplicado", "erro de fechamneto duplicado");

        assertTrue(score > 0.8, "expected a high score even with the typo, got " + score);
    }

    @Test
    void score_doesNotConfuseDifferentShortWords() {
        // "sim" and "nao" are the same length but opposite meanings: they must not become typos of each other.
        // If confused, the score would be 4/4=1.0 instead of 3/4.
        double score = TextSimilarity.score("usuario respondeu sim no formulario", "usuario respondeu nao no formulario");

        assertEquals(0.75, score);
    }

    @Test
    void score_ticketWithTooFewTokens_returnsZeroEvenIfEverythingMatches() {
        // Guards against inflated containment: 1 or 2 tokens matching by chance shouldn't
        // "cover" a whole Standard with 100% confidence.
        double score = TextSimilarity.score("erro", "ocorre um erro generico no sistema inteiro");

        assertEquals(0.0, score);
    }

    @Test
    void score_isAsymmetric_shortTicketContainedInARichStandardScoresHigh() {
        String ticketText = "cliente de outro estado nao consegue incluir item no pedido";
        String richStandardText = "rotina 4116 pode falhar de duas formas: sem preco ao buscar produto, "
                + "ou cliente de outro estado nao consegue incluir item no pedido por erro de tributacao";

        double score = TextSimilarity.score(ticketText, richStandardText);

        assertTrue(score >= 0.4, "a short ticket contained in a rich Standard should score high, got " + score);
    }
}
