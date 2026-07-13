package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.application.port.out.FeedbackRepositoryPort;
import com.knowledgeSupport.api.domain.model.Feedback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class FeedbackPersistenceAdapter implements FeedbackRepositoryPort {

    private final FeedbackJpaRepository feedbackJpaRepository;

    public FeedbackPersistenceAdapter(FeedbackJpaRepository feedbackJpaRepository) {
        this.feedbackJpaRepository = feedbackJpaRepository;
    }

    @Override
    public Feedback save(Feedback feedback) {
        FeedbackJpaEntity saved = feedbackJpaRepository.save(FeedbackMapper.toEntity(feedback));
        return FeedbackMapper.toDomain(saved);
    }

    @Override
    public List<Feedback> findByStandardId(UUID standardId) {
        return feedbackJpaRepository.findByStandardId(standardId).stream().map(FeedbackMapper::toDomain).toList();
    }
}
