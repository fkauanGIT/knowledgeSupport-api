package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.domain.model.Called;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalledServiceTest {

    @Mock
    private CalledProviderPort calledProviderPort;

    @Test
    void listOpenCalleds_delegatesToTheProviderPort() {
        Called called = Called.builder().titleCalled("test").build();
        when(calledProviderPort.fetchOpenCalleds()).thenReturn(List.of(called));

        CalledService service = new CalledService(calledProviderPort);

        assertEquals(1, service.listOpenCalleds().size());
    }
}
