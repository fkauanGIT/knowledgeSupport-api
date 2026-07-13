package com.knowledgeSupport.api.domain.model;

import java.util.List;

/**
 * Resultado agregado de rodar a análise em todos os chamados abertos e olhar só
 * os que deram NONE — onde cadastrar um Standard novo rende mais cobertura.
 */
public class GapReport {
    private final int totalChamadosAnalisados;
    private final int totalSemMatch;
    private final List<RoutineGap> lacunasPorRotina; // ordenado por quantidade desc

    public GapReport(int totalChamadosAnalisados, int totalSemMatch, List<RoutineGap> lacunasPorRotina) {
        this.totalChamadosAnalisados = totalChamadosAnalisados;
        this.totalSemMatch = totalSemMatch;
        this.lacunasPorRotina = lacunasPorRotina;
    }

    public int getTotalChamadosAnalisados() {
        return totalChamadosAnalisados;
    }

    public int getTotalSemMatch() {
        return totalSemMatch;
    }

    public List<RoutineGap> getLacunasPorRotina() {
        return lacunasPorRotina;
    }
}
