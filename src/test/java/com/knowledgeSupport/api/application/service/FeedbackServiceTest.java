package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.out.FeedbackRepositoryPort;
import com.knowledgeSupport.api.application.port.out.StandardRepositoryPort;
import com.knowledgeSupport.api.domain.model.Feedback;
import com.knowledgeSupport.api.domain.model.StandardAccuracy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepositoryPort feedbackRepositoryPort;

    @Mock
    private StandardRepositoryPort standardRepositoryPort;

    private FeedbackService service() {
        return new FeedbackService(feedbackRepositoryPort, standardRepositoryPort);
    }

    @Test
    void submit_lancaExcecaoQuandoStandardNaoExiste() {
        UUID standardId = UUID.randomUUID();
        when(standardRepositoryPort.existsById(standardId)).thenReturn(false);

        assertThrows(java.util.NoSuchElementException.class, () -> service().submit("SUP-1", standardId, true));
    }

    @Test
    void submit_salvaFeedbackQuandoStandardExiste() {
        UUID standardId = UUID.randomUUID();
        when(standardRepositoryPort.existsById(standardId)).thenReturn(true);
        when(feedbackRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);

        service().submit("SUP-1", standardId, true);

        org.mockito.Mockito.verify(feedbackRepositoryPort).save(captor.capture());
        assertEquals("SUP-1", captor.getValue().getJiraKey());
        assertEquals(standardId, captor.getValue().getStandardId());
        assertEquals(true, captor.getValue().isResolved());
    }

    @Test
    void accuracy_calculaTaxaDeAcertoComBaseNosFeedbacks() {
        UUID standardId = UUID.randomUUID();
        when(standardRepositoryPort.existsById(standardId)).thenReturn(true);
        when(feedbackRepositoryPort.findByStandardId(standardId)).thenReturn(List.of(
                new Feedback(UUID.randomUUID(), "SUP-1", standardId, true, new Date()),
                new Feedback(UUID.randomUUID(), "SUP-2", standardId, true, new Date()),
                new Feedback(UUID.randomUUID(), "SUP-3", standardId, false, new Date())
        ));

        StandardAccuracy accuracy = service().getAccuracy(standardId);

        assertEquals(3, accuracy.getTotalFeedbacks());
        assertEquals(2, accuracy.getResolvedCount());
        assertEquals(2.0 / 3.0, accuracy.getAccuracyRate(), 0.0001);
    }

    @Test
    void accuracy_semFeedback_devolveZeroSemQuebrar() {
        UUID standardId = UUID.randomUUID();
        when(standardRepositoryPort.existsById(standardId)).thenReturn(true);
        when(feedbackRepositoryPort.findByStandardId(standardId)).thenReturn(List.of());

        StandardAccuracy accuracy = service().getAccuracy(standardId);

        assertEquals(0, accuracy.getTotalFeedbacks());
        assertEquals(0.0, accuracy.getAccuracyRate());
    }

    @Test
    void accuracy_lancaExcecaoQuandoStandardNaoExiste() {
        UUID standardId = UUID.randomUUID();
        when(standardRepositoryPort.existsById(standardId)).thenReturn(false);

        assertThrows(java.util.NoSuchElementException.class, () -> service().getAccuracy(standardId));
    }
}
