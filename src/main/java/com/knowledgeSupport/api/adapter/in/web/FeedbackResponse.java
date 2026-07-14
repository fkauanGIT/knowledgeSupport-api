package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.Feedback;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.UUID;

@Schema(description = "Recorded feedback")
public record FeedbackResponse(
        UUID id,
        String jiraKey,
        UUID standardId,
        boolean resolved,
        Date createdAt) {

    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(feedback.getId(), feedback.getJiraKey(), feedback.getStandardId(), feedback.isResolved(), feedback.getCreatedAt());
    }
}
