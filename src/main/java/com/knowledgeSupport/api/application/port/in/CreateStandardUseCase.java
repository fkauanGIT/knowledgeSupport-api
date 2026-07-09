package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.Standard;

public interface CreateStandardUseCase {
    Standard create(Standard standard);
}
