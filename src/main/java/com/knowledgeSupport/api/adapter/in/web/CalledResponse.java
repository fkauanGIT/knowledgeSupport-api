package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.enums.FilterCategory;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;

import java.util.Date;

/**
 * Formato do JSON que a NOSSA API devolve (fronteira web, lado de saída da resposta).
 * Mesmo papel do StandardResponse.
 */
public record CalledResponse(String titleCalled,
                             String descriptionCalled,
                             String errorName,
                             IncidentType incidentType,
                             FilterCategory filterCategory,
                             String requesterName,
                             Date createdAt,
                             Date deadline,
                             Date updateAt) {

    public static CalledResponse from(Called called) {
        return new CalledResponse(
                called.getTitleCalled(),
                called.getDescriptionCalled(),
                called.getErrorName(),
                called.getIncidentType(),
                called.getFilterCategory(),
                called.getRequester() == null ? null : called.getRequester().getRequesterName(),
                called.getCreatedAt(),
                called.getDeadline(),
                called.getUpdateAt()
        );
    }
}
