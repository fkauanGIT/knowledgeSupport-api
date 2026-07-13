package com.knowledgeSupport.api.adapter.out.persistence;

import com.knowledgeSupport.api.domain.model.Feedback;

public final class FeedbackMapper {

    private FeedbackMapper() {}

    public static Feedback toDomain(FeedbackJpaEntity entity) {
        return new Feedback(entity.getId(), entity.getJiraKey(), entity.getStandardId(), entity.isResolved(), entity.getCreatedAt());
    }

    public static FeedbackJpaEntity toEntity(Feedback feedback) {
        return new FeedbackJpaEntity(feedback.getId(), feedback.getJiraKey(), feedback.getStandardId(), feedback.isResolved(), feedback.getCreatedAt());
    }
}
