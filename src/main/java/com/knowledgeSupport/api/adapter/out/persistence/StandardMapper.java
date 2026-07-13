package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.domain.model.Standard;

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
                .build();
    }

    public static StandardJpaEntity toEntity(Standard standard) {
        return new StandardJpaEntity(standard.getId(), standard.getStandardName(), standard.getText(), standard.getResult(), standard.getIncidentType(), standard.getRoutineNumber());
    }
}
