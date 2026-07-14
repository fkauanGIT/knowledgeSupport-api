package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.out.StandardRepositoryPort;
import com.knowledgeSupport.api.domain.model.Standard;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandardServiceTest {

    @Mock
    private StandardRepositoryPort standardRepositoryPort;

    private StandardService service() {
        return new StandardService(standardRepositoryPort);
    }

    private Standard standard(UUID id) {
        return Standard.builder().id(id).standardName("test").text("text").result("solution")
                .incidentType(IncidentType.ERROR).routineNumber(100).build();
    }

    @Test
    void create_delegatesToTheRepository() {
        Standard standard = standard(null);
        when(standardRepositoryPort.save(standard)).thenReturn(standard(UUID.randomUUID()));

        Standard created = service().create(standard);

        assertEquals("test", created.getStandardName());
    }

    @Test
    void update_throwsExceptionWhenItDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(standardRepositoryPort.existsById(id)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> service().update(id, standard(null)));
        verify(standardRepositoryPort, never()).save(any());
    }

    @Test
    void update_savesWithThePathIdWhenItExists() {
        UUID id = UUID.randomUUID();
        when(standardRepositoryPort.existsById(id)).thenReturn(true);
        when(standardRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Standard updated = service().update(id, standard(null));

        assertEquals(id, updated.getId());
    }

    @Test
    void deleteById_throwsExceptionWhenItDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(standardRepositoryPort.existsById(id)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> service().deleteById(id));
        verify(standardRepositoryPort, never()).deleteById(any());
    }

    @Test
    void getById_returnsEmptyWhenItDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(standardRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), service().getById(id));
    }

    @Test
    void listAll_delegatesToTheRepository() {
        when(standardRepositoryPort.findAll()).thenReturn(List.of(standard(UUID.randomUUID())));

        assertEquals(1, service().listAll().size());
    }
}
