package com.knowledgeSupport.api.application.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TfIdfSearchTest {

    @Test
    void score_rewardsDocumentsWhereQueryTermsAreRareAcrossTheCorpus() {
        // "faturamento" appears in every doc (low idf); "sefaz" only in doc 0 (high idf).
        List<List<String>> corpus = List.of(
                List.of("erro", "sefaz", "faturamento"),
                List.of("nota", "faturamento"),
                List.of("pedido", "faturamento")
        );

        double[] scores = TfIdfSearch.score(corpus, List.of("sefaz"));

        assertTrue(scores[0] > 0);
        assertEquals(0, scores[1]);
        assertEquals(0, scores[2]);
    }

    @Test
    void score_higherTermFrequencyScoresHigher() {
        List<List<String>> corpus = List.of(
                List.of("erro", "erro", "erro", "faturamento"),
                List.of("erro", "faturamento")
        );

        double[] scores = TfIdfSearch.score(corpus, List.of("erro"));

        assertTrue(scores[0] > scores[1]);
    }

    @Test
    void score_withEmptyCorpus_returnsEmptyArray() {
        double[] scores = TfIdfSearch.score(List.of(), List.of("erro"));

        assertEquals(0, scores.length);
    }

    @Test
    void score_withNoMatchingTerms_isAllZero() {
        List<List<String>> corpus = List.of(List.of("nota", "fiscal"));

        double[] scores = TfIdfSearch.score(corpus, List.of("sefaz"));

        assertEquals(0, scores[0]);
    }

    @Test
    void toRelevancePercentages_bestScoreBecomesHundred() {
        int[] relevances = TfIdfSearch.toRelevancePercentages(new double[]{2.0, 1.0, 0.5});

        assertEquals(100, relevances[0]);
        assertEquals(50, relevances[1]);
        assertEquals(25, relevances[2]);
    }

    @Test
    void toRelevancePercentages_allZeroScores_staysAllZero() {
        int[] relevances = TfIdfSearch.toRelevancePercentages(new double[]{0, 0, 0});

        assertEquals(0, relevances[0]);
        assertEquals(0, relevances[1]);
        assertEquals(0, relevances[2]);
    }
}
