package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.in.GetStandardAccuracyUseCase;
import com.knowledgeSupport.api.application.port.in.SubmitFeedbackUseCase;
import com.knowledgeSupport.api.application.port.out.FeedbackRepositoryPort;
import com.knowledgeSupport.api.application.port.out.StandardRepositoryPort;
import com.knowledgeSupport.api.domain.model.Feedback;
import com.knowledgeSupport.api.domain.model.StandardAccuracy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class FeedbackService implements SubmitFeedbackUseCase, GetStandardAccuracyUseCase {

    private final FeedbackRepositoryPort feedbackRepositoryPort;
    private final StandardRepositoryPort standardRepositoryPort;

    public FeedbackService(FeedbackRepositoryPort feedbackRepositoryPort, StandardRepositoryPort standardRepositoryPort) {
        this.feedbackRepositoryPort = feedbackRepositoryPort;
        this.standardRepositoryPort = standardRepositoryPort;
    }

    @Override
    public Feedback submit(String jiraKey, UUID standardId, boolean resolved) {
        if (!standardRepositoryPort.existsById(standardId)) {
            throw new NoSuchElementException("Standard not found: " + standardId);
        }
        Feedback feedback = new Feedback(null, jiraKey, standardId, resolved, new Date());
        return feedbackRepositoryPort.save(feedback);
    }

    @Override
    public StandardAccuracy getAccuracy(UUID standardId) {
        if (!standardRepositoryPort.existsById(standardId)) {
            throw new NoSuchElementException("Standard not found: " + standardId);
        }
        List<Feedback> feedbacks = feedbackRepositoryPort.findByStandardId(standardId);
        int total = feedbacks.size();
        long resolvedCount = feedbacks.stream().filter(Feedback::isResolved).count();
        double accuracyRate = total == 0 ? 0.0 : (double) resolvedCount / total;
        return new StandardAccuracy(standardId, total, (int) resolvedCount, accuracyRate);
    }
}
