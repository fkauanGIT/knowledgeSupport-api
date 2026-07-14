package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledAnalysis;
import com.knowledgeSupport.api.domain.model.MatchMethod;
import com.knowledgeSupport.api.domain.model.Standard;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The matching cascade itself (exact -> score by routine -> overall score -> NONE), with no
 * Spring and no ports: receives an already-fetched Called and the already-loaded list of
 * Standards. Extracted from AnalyzeCalledService so it can be reused by whoever needs to run
 * the same cascade in bulk (GapReportService) without paying the cost of re-fetching the
 * Called/Standards on every ticket.
 */
public class CalledStandardMatcher {

    private final double threshold;

    public CalledStandardMatcher(double threshold) {
        this.threshold = threshold;
    }

    public CalledAnalysis match(Called called, List<Standard> standards) {
        List<Standard> candidates = standards.stream()
                .filter(CalledStandardMatcher::hasSolution)
                .toList();

        Optional<Standard> exactMatch = candidates.stream()
                .filter(standard -> sameRoutineAndSameError(called, standard))
                .findFirst();
        if (exactMatch.isPresent()) {
            return new CalledAnalysis(called, exactMatch.get(), MatchMethod.of("ROUTINE_AND_ERROR_NAME", 1.0));
        }

        // routineNumber is a filter (reduces candidates), not a mandatory pair: if it narrows
        // the search and candidates remain, prioritize that subset before opening up to the whole base.
        List<Standard> routineCandidates = candidates.stream()
                .filter(standard -> sameRoutine(called, standard))
                .toList();

        if (!routineCandidates.isEmpty()) {
            Optional<CalledAnalysis> byRoutineAndScore = bestByScore(called, routineCandidates, "ROUTINE_AND_TEXT_SCORE");
            if (byRoutineAndScore.isPresent()) {
                return byRoutineAndScore.get();
            }
        }

        Optional<CalledAnalysis> byScore = bestByScore(called, candidates, "TEXT_SCORE");
        return byScore.orElseGet(() -> new CalledAnalysis(called, null, MatchMethod.none()));
    }

    private Optional<CalledAnalysis> bestByScore(Called called, List<Standard> candidates, String methodName) {
        String ticketText = String.join(" ",
                nullToEmpty(called.getTitleCalled()), nullToEmpty(called.getDescriptionCalled()), nullToEmpty(called.getErrorName()));

        return candidates.stream()
                .map(standard -> {
                    String standardText = String.join(" ", nullToEmpty(standard.getStandardName()), nullToEmpty(standard.getText()));
                    double score = TextSimilarity.score(ticketText, standardText);
                    return new ScoredStandard(standard, score);
                })
                .filter(scored -> scored.score() >= threshold)
                .max(Comparator.comparingDouble(ScoredStandard::score))
                .map(scored -> new CalledAnalysis(called, scored.standard(), MatchMethod.of(methodName, scored.score())));
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
