package com.knowledgeSupport.api.application.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Plain TF-IDF scoring — keyword search, not embeddings/AI. Ported from the desktop app's
 * original local implementation (see the backend spec's "Algoritmo de referência"); this is
 * a deliberate baseline, not a placeholder for a smarter version.
 */
public final class TfIdfSearch {

    private TfIdfSearch() {}

    /**
     * Scores each document in {@code corpus} against {@code queryTokens}.
     * {@code score[i] = sum over query terms of tf(term, corpus[i]) * idf(term)}, with
     * {@code idf(term) = ln(1 + totalDocs / max(df(term), 1))}.
     */
    public static double[] score(List<List<String>> corpus, List<String> queryTokens) {
        int totalDocs = corpus.size();
        double[] scores = new double[totalDocs];
        if (totalDocs == 0 || queryTokens.isEmpty()) {
            return scores;
        }

        List<Set<String>> corpusTermSets = corpus.stream().map(HashSet::new).map(s -> (Set<String>) s).toList();
        Set<String> uniqueQueryTerms = new HashSet<>(queryTokens);
        Map<String, Integer> documentFrequency = new HashMap<>();
        for (String term : uniqueQueryTerms) {
            int count = 0;
            for (Set<String> docTerms : corpusTermSets) {
                if (docTerms.contains(term)) {
                    count++;
                }
            }
            documentFrequency.put(term, count);
        }

        for (int i = 0; i < totalDocs; i++) {
            Map<String, Long> termFrequency = new HashMap<>();
            for (String token : corpus.get(i)) {
                termFrequency.merge(token, 1L, Long::sum);
            }
            double score = 0;
            for (String term : queryTokens) {
                long tf = termFrequency.getOrDefault(term, 0L);
                if (tf == 0) continue;
                int df = documentFrequency.getOrDefault(term, 0);
                double idf = Math.log(1.0 + (double) totalDocs / Math.max(df, 1));
                score += tf * idf;
            }
            scores[i] = score;
        }
        return scores;
    }

    /** Best score becomes 100; everything else is relative to it. All-zero input stays all-zero. */
    public static int[] toRelevancePercentages(double[] scores) {
        double best = 0;
        for (double s : scores) {
            best = Math.max(best, s);
        }
        int[] percentages = new int[scores.length];
        if (best <= 0) {
            return percentages;
        }
        for (int i = 0; i < scores.length; i++) {
            percentages[i] = (int) Math.round(100.0 * scores[i] / best);
        }
        return percentages;
    }
}
