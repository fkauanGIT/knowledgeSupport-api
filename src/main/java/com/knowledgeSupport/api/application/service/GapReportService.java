package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.in.GapReportUseCase;
import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.application.port.out.StandardRepositoryPort;
import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledAnalysis;
import com.knowledgeSupport.api.domain.model.GapReport;
import com.knowledgeSupport.api.domain.model.RoutineGap;
import com.knowledgeSupport.api.domain.model.Standard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Runs the same cascade as AnalyzeCalledService over every open ticket, but fetches
 * Called/Standard only ONCE (not per ticket) — avoids N+1 against Jira and the database when
 * the report runs over a large list.
 */
@Service
public class GapReportService implements GapReportUseCase {

    private static final int MAX_EXAMPLES_PER_ROUTINE = 3;

    private final CalledProviderPort calledProviderPort;
    private final StandardRepositoryPort standardRepositoryPort;
    private final CalledStandardMatcher matcher;

    public GapReportService(CalledProviderPort calledProviderPort,
                             StandardRepositoryPort standardRepositoryPort,
                             @Value("${matching.threshold:0.4}") double threshold,
                             @Value("${matching.high-confidence-threshold:0.75}") double highConfidenceThreshold) {
        this.calledProviderPort = calledProviderPort;
        this.standardRepositoryPort = standardRepositoryPort;
        this.matcher = new CalledStandardMatcher(threshold, highConfidenceThreshold);
    }

    @Override
    public GapReport generate() {
        List<Called> calleds = calledProviderPort.fetchOpenCalleds();
        List<Standard> standards = standardRepositoryPort.findAll();

        List<Called> withoutMatch = calleds.stream()
                .map(called -> matcher.match(called, standards))
                .filter(analysis -> "NONE".equals(analysis.getMethod().getName()))
                .map(CalledAnalysis::getCalled)
                .toList();

        // Collectors.groupingBy rejects a null key (NullPointerException) — routineNumber
        // can be null (a ticket with no routine filled in on Jira), so group by Optional.
        Map<Optional<Integer>, List<Called>> byRoutine = withoutMatch.stream()
                .collect(Collectors.groupingBy(called -> Optional.ofNullable(called.getRoutineNumber())));

        List<RoutineGap> gaps = byRoutine.entrySet().stream()
                .map(entry -> toRoutineGap(entry.getKey().orElse(null), entry.getValue(), withoutMatch.size()))
                .sorted(Comparator.comparingInt(RoutineGap::getCount).reversed())
                .toList();

        return new GapReport(calleds.size(), withoutMatch.size(), gaps);
    }

    private RoutineGap toRoutineGap(Integer routineNumber, List<Called> routineCalleds, int totalWithoutMatch) {
        double percentage = totalWithoutMatch == 0 ? 0.0 : (100.0 * routineCalleds.size() / totalWithoutMatch);
        List<String> examples = routineCalleds.stream()
                .map(Called::getTitleCalled)
                .filter(title -> title != null && !title.isBlank())
                .distinct()
                .limit(MAX_EXAMPLES_PER_ROUTINE)
                .toList();
        return new RoutineGap(routineNumber, routineCalleds.size(), percentage, examples);
    }
}
