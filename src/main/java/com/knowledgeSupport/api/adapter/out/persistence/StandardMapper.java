package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.domain.model.InvestigationStep;
import com.knowledgeSupport.api.domain.model.Standard;

import java.util.List;

public final class StandardMapper {

    private StandardMapper() {}

    public static Standard toDomain(StandardJpaEntity entity) {
        return Standard.builder()
                .id(entity.getId())
                .standardName(entity.getStandardName())
                .text(entity.getText())
                .result(entity.getResult())
                .incidentType(entity.getIncidentType())
                .routineNumber(entity.getRoutineNumber())
                .investigationSteps(toDomainSteps(entity.getInvestigationSteps()))
                .build();
    }

    public static StandardJpaEntity toEntity(Standard standard) {
        return new StandardJpaEntity(standard.getId(), standard.getStandardName(), standard.getText(), standard.getResult(),
                standard.getIncidentType(), standard.getRoutineNumber(), toEntitySteps(standard.getInvestigationSteps()));
    }

    private static List<InvestigationStep> toDomainSteps(List<InvestigationStepEmbeddable> entities) {
        return entities.stream()
                .map(step -> InvestigationStep.builder()
                        .hypothesis(step.getHypothesis())
                        .query(step.getQuery())
                        .verification(step.getVerification())
                        .confirmed(step.isConfirmed())
                        .build())
                .toList();
    }

    private static List<InvestigationStepEmbeddable> toEntitySteps(List<InvestigationStep> steps) {
        return steps.stream()
                .map(step -> new InvestigationStepEmbeddable(step.getHypothesis(), step.getQuery(), step.getVerification(), step.isConfirmed()))
                .toList();
    }
}
