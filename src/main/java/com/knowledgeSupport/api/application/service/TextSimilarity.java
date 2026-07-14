package com.knowledgeSupport.api.application.service;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Pure text comparison (no Spring, no I/O): normalizes, tokenizes and measures how much of
 * the ticket's text is covered by the Standard's text, tolerating typos.
 * Used by AnalyzeCalledService to score Called x Standard when exact equality on
 * errorName doesn't resolve it.
 */
public final class TextSimilarity {

    // Deliberately small, fixed list: only the most common Portuguese words that carry no
    // business meaning. Grows on demand, doesn't need to be exhaustive. Kept in Portuguese
    // on purpose — this stopword list matches the language of the real support tickets the
    // matcher processes, not the language the code is written in (see docs/LIMITATIONS.md).
    // "nao" is deliberately NOT in this list: it's negation, not noise — removing it would
    // change the meaning ("caixa fecha" x "caixa nao fecha" are opposite problems, not synonyms).
    private static final Set<String> STOPWORDS_PT = Set.of(
            "a", "o", "as", "os", "de", "do", "da", "dos", "das", "em", "no", "na", "nos", "nas",
            "um", "uma", "uns", "umas", "e", "ou", "que", "com", "sem", "para", "por", "pra",
            "ao", "aos", "à", "às", "se", "sua", "seu", "suas", "seus", "ja", "mas", "como",
            "quando", "depois", "antes", "esta", "este", "esse", "essa", "isso", "foi", "ser",
            "tem", "ha", "pelo", "pela", "num", "numa", "ate"
    );

    private static final LevenshteinDistance LEVENSHTEIN = LevenshteinDistance.getDefaultInstance();

    // A ticket with too few tokens doesn't carry enough information to support a reliable
    // containment score (e.g. 1 token matching by chance shouldn't "cover" a whole Standard).
    private static final int MIN_TICKET_TOKENS = 3;

    private TextSimilarity() {}

    public static String normalize(String value) {
        if (value == null) return "";
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    public static Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) return Set.of();
        return Arrays.stream(normalize(text).split("[^\\p{L}\\p{N}]+"))
                .filter(token -> !token.isBlank())
                .filter(token -> !STOPWORDS_PT.contains(token))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    /**
     * Containment score, not symmetric Jaccard: measures how much of the TICKET is covered by
     * the Standard's text (intersection / ticket tokens), with typo tolerance. Asymmetric on
     * purpose — the ticket tends to be short and the Standard grows over time (accumulates
     * symptom variations); dividing by the union would penalize rich Standards, which is
     * exactly the behavior we want to encourage. Result between 0 and 1; ticketText needs
     * to come first, argument order matters.
     */
    public static double score(String ticketText, String standardText) {
        Set<String> ticketTokens = tokenize(ticketText);
        Set<String> standardTokens = tokenize(standardText);

        if (ticketTokens.size() < MIN_TICKET_TOKENS || standardTokens.isEmpty()) return 0.0;

        Set<String> availableStandardTokens = new HashSet<>(standardTokens);
        int matches = 0;
        for (String ticketToken : ticketTokens) {
            String found = null;
            for (String standardToken : availableStandardTokens) {
                if (isFuzzyMatch(ticketToken, standardToken)) {
                    found = standardToken;
                    break;
                }
            }
            if (found != null) {
                availableStandardTokens.remove(found);
                matches++;
            }
        }

        return (double) matches / ticketTokens.size();
    }

    private static boolean isFuzzyMatch(String a, String b) {
        if (a.equals(b)) return true;
        int shorter = Math.min(a.length(), b.length());
        if (shorter <= 2) return false; // token too short: only accept exact equality
        int longer = Math.max(a.length(), b.length());
        int tolerance = longer <= 4 ? 1 : 2;
        return LEVENSHTEIN.apply(a, b) <= tolerance;
    }
}
