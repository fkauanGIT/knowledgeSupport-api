package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledAnalysis;
import com.knowledgeSupport.api.domain.model.MatchMethod;
import com.knowledgeSupport.api.domain.model.Standard;
import com.knowledgeSupport.api.domain.model.enums.Confidence;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The matching cascade itself (exact -> score by routine -> overall score -> NONE), with no
 * Spring and no ports. Extracted from AnalyzeCalledService so GapReportService can run the same
 * cascade in bulk without re-fetching Called/Standards per ticket.
 *
 * <p>Otimização: cada Standard é tokenizado UMA vez ({@link #prepare}) e o ticket UMA vez por
 * análise. Antes, {@code TextSimilarity.score(String,String)} re-tokenizava o texto do Standard
 * para cada ticket e o do ticket para cada candidato — no gap-report, O(tickets × standards)
 * tokenizações redundantes. O score é idêntico (mesmo greedy sobre os mesmos tokens; fuzz de 300k).</p>
 */
public class CalledStandardMatcher {

    private final double threshold;
    private final double highConfidenceThreshold;

    public CalledStandardMatcher(double threshold, double highConfidenceThreshold) {
        this.threshold = threshold;
        this.highConfidenceThreshold = highConfidenceThreshold;
    }

    /** Standard com seus tokens (standardName + text) pré-calculados. */
    public record PreparedStandard(Standard standard, Set<String> tokens) {}

    /** Tokeniza os Standards com solução UMA vez, para reuso por vários tickets (gap-report). */
    public static List<PreparedStandard> prepare(List<Standard> standards) {
        return standards.stream()
                .filter(CalledStandardMatcher::hasSolution)
                .map(standard -> new PreparedStandard(
                        standard,
                        TextSimilarity.tokenize(nullToEmpty(standard.getStandardName()) + " " + nullToEmpty(standard.getText()))))
                .toList();
    }

    /** Conveniência: prepara e casa em um passo (análise de um único ticket). */
    public CalledAnalysis match(Called called, List<Standard> standards) {
        return matchPrepared(called, prepare(standards));
    }

    /** Cascata de matching sobre candidatos já tokenizados. */
    public CalledAnalysis matchPrepared(Called called, List<PreparedStandard> candidates) {
        Optional<PreparedStandard> exactMatch = candidates.stream()
                .filter(candidate -> sameRoutineAndSameError(called, candidate.standard()))
                .findFirst();
        if (exactMatch.isPresent()) {
            return new CalledAnalysis(called, exactMatch.get().standard(),
                    MatchMethod.of("ROUTINE_AND_ERROR_NAME", 1.0, Confidence.CONFIRMED));
        }

        Set<String> ticketTokens = TextSimilarity.tokenize(String.join(" ",
                nullToEmpty(called.getTitleCalled()),
                nullToEmpty(called.getDescriptionCalled()),
                nullToEmpty(called.getErrorName())));

        List<PreparedStandard> routineCandidates = candidates.stream()
                .filter(candidate -> sameRoutine(called, candidate.standard()))
                .toList();

        if (!routineCandidates.isEmpty()) {
            Optional<CalledAnalysis> byRoutineAndScore = bestByScore(called, ticketTokens, routineCandidates, "ROUTINE_AND_TEXT_SCORE");
            if (byRoutineAndScore.isPresent()) {
                return byRoutineAndScore.get();
            }
        }

        Optional<CalledAnalysis> byScore = bestByScore(called, ticketTokens, candidates, "TEXT_SCORE");
        return byScore.orElseGet(() -> new CalledAnalysis(called, null, MatchMethod.none()));
    }

    private Optional<CalledAnalysis> bestByScore(Called called, Set<String> ticketTokens,
                                                 List<PreparedStandard> candidates, String methodName) {
        return candidates.stream()
                .map(candidate -> new ScoredStandard(candidate.standard(),
                        TextSimilarity.score(ticketTokens, candidate.tokens())))
                .filter(scored -> scored.score() >= threshold)
                .max(Comparator.comparingDouble(ScoredStandard::score))
                .map(scored -> new CalledAnalysis(called, scored.standard(),
                        MatchMethod.of(methodName, scored.score(), confidenceFor(scored.score()))));
    }

    private Confidence confidenceFor(double score) {
        return score >= highConfidenceThreshold ? Confidence.LIKELY : Confidence.UNCERTAIN;
    }

    private boolean sameRoutine(Called called, Standard standard) {
        return called.getRoutineNumber() != null && called.getRoutineNumber().equals(standard.getRoutineNumber());
    }

    private boolean sameRoutineAndSameError(Called called, Standard standard) {
        boolean errorMatches = called.getErrorName() != null
                && standard.getStandardName() != null
                && TextSimilarity.normalize(called.getErrorName()).equals(TextSimilarity.normalize(standard.getStandardName()));
        return sameRoutine(called, standard) && errorMatches;
    }

    private static boolean hasSolution(Standard standard) {
        return standard.getResult() != null && !standard.getResult().isBlank();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record ScoredStandard(Standard standard, double score) {}
}
