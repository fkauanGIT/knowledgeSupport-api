package com.knowledgeSupport.api.application.service;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Normalizes and tokenizes text for the TF-IDF search: strip accents, lowercase, keep only
 * alphanumeric runs. Same rules for both the indexed chunks and the search query, or scores
 * wouldn't be comparable.
 */
public final class TextTokenizer {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern TOKEN = Pattern.compile("[a-z0-9]+");

    private TextTokenizer() {}

    public static String normalize(String text) {
        if (text == null) return "";
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed).replaceAll("").toLowerCase();
    }

    public static List<String> tokenize(String text) {
        String normalized = normalize(text);
        return TOKEN.matcher(normalized).results().map(m -> m.group()).toList();
    }
}
