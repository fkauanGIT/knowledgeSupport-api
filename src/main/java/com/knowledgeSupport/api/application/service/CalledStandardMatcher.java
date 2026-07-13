package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledAnalysis;
import com.knowledgeSupport.api.domain.model.MatchMethod;
import com.knowledgeSupport.api.domain.model.Standard;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * A cascata de match em si (exato -> score por rotina -> score geral -> NONE), sem Spring
 * e sem ports: recebe um Called já buscado e a lista de Standards já carregada. Extraído de
 * AnalyzeCalledService pra ser reaproveitado por quem precisa rodar a mesma cascata em lote
 * (GapReportService) sem pagar o custo de re-buscar o Called/Standards a cada chamado.
 */
public class CalledStandardMatcher {

    private final double threshold;

    public CalledStandardMatcher(double threshold) {
        this.threshold = threshold;
    }

    public CalledAnalysis match(Called called, List<Standard> standards) {
        List<Standard> candidatos = standards.stream()
                .filter(CalledStandardMatcher::temSolucao)
                .toList();

        Optional<Standard> matchExato = candidatos.stream()
                .filter(standard -> mesmaRotinaEMesmoErro(called, standard))
                .findFirst();
        if (matchExato.isPresent()) {
            return new CalledAnalysis(called, matchExato.get(), MatchMethod.of("ROUTINE_AND_ERROR_NAME", 1.0));
        }

        // routineNumber é filtro (reduz candidatos), não par obrigatório: se restringir a busca
        // e ainda sobrar candidato, prioriza esse subconjunto antes de abrir pra base inteira.
        List<Standard> candidatosDaRotina = candidatos.stream()
                .filter(standard -> mesmaRotina(called, standard))
                .toList();

        if (!candidatosDaRotina.isEmpty()) {
            Optional<CalledAnalysis> porRotinaEScore = melhorPorScore(called, candidatosDaRotina, "ROUTINE_AND_TEXT_SCORE");
            if (porRotinaEScore.isPresent()) {
                return porRotinaEScore.get();
            }
        }

        Optional<CalledAnalysis> porScore = melhorPorScore(called, candidatos, "TEXT_SCORE");
        return porScore.orElseGet(() -> new CalledAnalysis(called, null, MatchMethod.none()));
    }

    private Optional<CalledAnalysis> melhorPorScore(Called called, List<Standard> candidatos, String nomeDoMetodo) {
        String textoChamado = String.join(" ",
                nullToEmpty(called.getTitleCalled()), nullToEmpty(called.getDescriptionCalled()), nullToEmpty(called.getErrorName()));

        return candidatos.stream()
                .map(standard -> {
                    String textoStandard = String.join(" ", nullToEmpty(standard.getStandardName()), nullToEmpty(standard.getText()));
                    double score = TextSimilarity.score(textoChamado, textoStandard);
                    return new ScoredStandard(standard, score);
                })
                .filter(scored -> scored.score() >= threshold)
                .max(Comparator.comparingDouble(ScoredStandard::score))
                .map(scored -> new CalledAnalysis(called, scored.standard(), MatchMethod.of(nomeDoMetodo, scored.score())));
    }

    private boolean mesmaRotina(Called called, Standard standard) {
        return called.getRoutineNumber() != null && called.getRoutineNumber().equals(standard.getRoutineNumber());
    }

    private boolean mesmaRotinaEMesmoErro(Called called, Standard standard) {
        boolean erroBate = called.getErrorName() != null
                && standard.getStandardName() != null
                && TextSimilarity.normalize(called.getErrorName()).equals(TextSimilarity.normalize(standard.getStandardName()));

        return mesmaRotina(called, standard) && erroBate;
    }

    private static boolean temSolucao(Standard standard) {
        return standard.getResult() != null && !standard.getResult().isBlank();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record ScoredStandard(Standard standard, double score) {}
}
