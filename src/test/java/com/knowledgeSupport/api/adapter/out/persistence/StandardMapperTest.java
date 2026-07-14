package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.domain.model.Standard;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardMapperTest {

    @Test
    void toDomainAndToEntity_roundTripWithoutLosingAnyField() {
        UUID id = UUID.randomUUID();
        Standard original = Standard.builder()
                .id(id)
                .standardName("name")
                .text("text")
                .result("result")
                .incidentType(IncidentType.ALERT)
                .routineNumber(4116)
                .build();

        StandardJpaEntity entity = StandardMapper.toEntity(original);
        Standard backToDomain = StandardMapper.toDomain(entity);

        assertEquals(id, backToDomain.getId());
        assertEquals("name", backToDomain.getStandardName());
        assertEquals("text", backToDomain.getText());
        assertEquals("result", backToDomain.getResult());
        assertEquals(IncidentType.ALERT, backToDomain.getIncidentType());
        assertEquals(4116, backToDomain.getRoutineNumber());
    }
}
