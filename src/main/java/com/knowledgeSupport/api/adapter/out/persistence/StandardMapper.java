package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.domain.model.Standard;

public final class StandardMapper {

    private StandardMapper() {}

    public static Standard toDomain(StandardJpaEntity entity) {
        return new Standard(entity.getId(), entity.getStandardName(), entity.getText(), entity.getResult(), entity.getIncidentType());
    }

    public static StandardJpaEntity toEntity(Standard standard) {
        return new StandardJpaEntity(standard.getId(), standard.getStandardName(), standard.getText(), standard.getResult(), standard.getIncidentType());
    }
}
