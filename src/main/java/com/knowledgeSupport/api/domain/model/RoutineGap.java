package com.knowledgeSupport.api.domain.model;

import java.util.List;

/**
 * Quantos chamados de uma rotina não encontraram Standard, e o quanto isso representa
 * do total de lacunas — a resposta pra "cadastre isto aqui e cubra X% do volume".
 */
public class RoutineGap {
    private final Integer routineNumber; // null = chamados sem rotina preenchida
    private final int quantidade;
    private final double percentualDasLacunas;
    private final List<String> exemplos;

    public RoutineGap(Integer routineNumber, int quantidade, double percentualDasLacunas, List<String> exemplos) {
        this.routineNumber = routineNumber;
        this.quantidade = quantidade;
        this.percentualDasLacunas = percentualDasLacunas;
        this.exemplos = exemplos;
    }

    public Integer getRoutineNumber() {
        return routineNumber;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getPercentualDasLacunas() {
        return percentualDasLacunas;
    }

    public List<String> getExemplos() {
        return exemplos;
    }
}
