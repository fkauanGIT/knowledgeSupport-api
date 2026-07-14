package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.domain.model.InvestigationStep;
import com.knowledgeSupport.api.domain.model.Standard;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardMapperTest {

    @Test
    void toDomainAndToEntity_roundTripWithoutLosingAnyField() {
        UUID id = UUID.randomUUID();
        InvestigationStep discarded = InvestigationStep.builder()
                .hypothesis("Wrong branch config")
                .query("SELECT * FROM pcconfig WHERE codfilial = 1")
                .verification("Config row existed and was correct — discarded")
                .confirmed(false)
                .build();
        InvestigationStep confirmedStep = InvestigationStep.builder()
                .hypothesis("Fiscal configuration out of sync")
                .query("SELECT * FROM pcconfig WHERE codfilial = 2")
                .verification("Config row was missing — confirmed")
                .confirmed(true)
                .build();

        Standard original = Standard.builder()
                .id(id)
                .standardName("name")
                .text("text")
                .result("result")
                .incidentType(IncidentType.ALERT)
                .routineNumber(4116)
                .investigationSteps(List.of(discarded, confirmedStep))
                .build();

        StandardJpaEntity entity = StandardMapper.toEntity(original);
        Standard backToDomain = StandardMapper.toDomain(entity);

        assertEquals(id, backToDomain.getId());
        assertEquals("name", backToDomain.getStandardName());
        assertEquals("text", backToDomain.getText());
        assertEquals("result", backToDomain.getResult());
        assertEquals(IncidentType.ALERT, backToDomain.getIncidentType());
        assertEquals(4116, backToDomain.getRoutineNumber());

        assertEquals(2, backToDomain.getInvestigationSteps().size());
        assertEquals("Wrong branch config", backToDomain.getInvestigationSteps().get(0).getHypothesis());
        assertFalse(backToDomain.getInvestigationSteps().get(0).isConfirmed());
        assertEquals("Fiscal configuration out of sync", backToDomain.getInvestigationSteps().get(1).getHypothesis());
        assertTrue(backToDomain.getInvestigationSteps().get(1).isConfirmed());
    }
}
