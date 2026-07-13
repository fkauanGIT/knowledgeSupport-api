package com.knowledgeSupport.api.application.port.out;

import com.knowledgeSupport.api.domain.model.Feedback;

import java.util.List;
import java.util.UUID;

public interface FeedbackRepositoryPort {
    Feedback save(Feedback feedback);

    List<Feedback> findByStandardId(UUID standardId);
}
