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
 * Roda a mesma cascata do AnalyzeCalledService em todos os chamados abertos, mas busca
 * Called/Standard UMA vez só (não por chamado) — evita N+1 no Jira e no banco quando o
 * relatório roda sobre uma lista grande.
 */
@Service
public class GapReportService implements GapReportUseCase {

    private static final int MAX_EXEMPLOS_POR_ROTINA = 3;

    private final CalledProviderPort calledProviderPort;
    private final StandardRepositoryPort standardRepositoryPort;
    private final CalledStandardMatcher matcher;

    public GapReportService(CalledProviderPort calledProviderPort,
                             StandardRepositoryPort standardRepositoryPort,
                             @Value("${matching.threshold:0.4}") double threshold) {
        this.calledProviderPort = calledProviderPort;
        this.standardRepositoryPort = standardRepositoryPort;
        this.matcher = new CalledStandardMatcher(threshold);
    }

    @Override
    public GapReport generate() {
        List<Called> calleds = calledProviderPort.fetchOpenCalleds();
        List<Standard> standards = standardRepositoryPort.findAll();

        List<Called> semMatch = calleds.stream()
                .map(called -> matcher.match(called, standards))
                .filter(analysis -> "NONE".equals(analysis.getMethod().getName()))
                .map(CalledAnalysis::getCalled)
                .toList();

        // Collectors.groupingBy rejeita chave nula (NullPointerException) — routineNumber
        // pode ser null (chamado sem rotina preenchida no Jira), então agrupa por Optional.
        Map<Optional<Integer>, List<Called>> porRotina = semMatch.stream()
                .collect(Collectors.groupingBy(called -> Optional.ofNullable(called.getRoutineNumber())));

        List<RoutineGap> lacunas = porRotina.entrySet().stream()
                .map(entry -> toRoutineGap(entry.getKey().orElse(null), entry.getValue(), semMatch.size()))
                .sorted(Comparator.comparingInt(RoutineGap::getQuantidade).reversed())
                .toList();

        return new GapReport(calleds.size(), semMatch.size(), lacunas);
    }

    private RoutineGap toRoutineGap(Integer routineNumber, List<Called> chamadosDaRotina, int totalSemMatch) {
        double percentual = totalSemMatch == 0 ? 0.0 : (100.0 * chamadosDaRotina.size() / totalSemMatch);
        List<String> exemplos = chamadosDaRotina.stream()
                .map(Called::getTitleCalled)
                .filter(titulo -> titulo != null && !titulo.isBlank())
                .distinct()
                .limit(MAX_EXEMPLOS_POR_ROTINA)
                .toList();
        return new RoutineGap(routineNumber, chamadosDaRotina.size(), percentual, exemplos);
    }
}
