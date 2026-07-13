package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.domain.model.Standard;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardMapperTest {

    @Test
    void toDomainEToEntity_fazemORoundTripSemPerderCampo() {
        UUID id = UUID.randomUUID();
        Standard original = Standard.builder()
                .id(id)
                .standardName("nome")
                .text("texto")
                .result("resultado")
                .incidentType(IncidentType.ALERT)
                .routineNumber(4116)
                .build();

        StandardJpaEntity entity = StandardMapper.toEntity(original);
        Standard voltaAoDominio = StandardMapper.toDomain(entity);

        assertEquals(id, voltaAoDominio.getId());
        assertEquals("nome", voltaAoDominio.getStandardName());
        assertEquals("texto", voltaAoDominio.getText());
        assertEquals("resultado", voltaAoDominio.getResult());
        assertEquals(IncidentType.ALERT, voltaAoDominio.getIncidentType());
        assertEquals(4116, voltaAoDominio.getRoutineNumber());
    }
}
