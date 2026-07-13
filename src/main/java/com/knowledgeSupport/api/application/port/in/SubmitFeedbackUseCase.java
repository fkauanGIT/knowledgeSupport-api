package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.Feedback;

import java.util.UUID;

public interface SubmitFeedbackUseCase {
    Feedback submit(String jiraKey, UUID standardId, boolean resolved);
}
