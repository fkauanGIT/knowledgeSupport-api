package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.StandardAccuracy;

import java.util.UUID;

public interface GetStandardAccuracyUseCase {
    StandardAccuracy getAccuracy(UUID standardId);
}
