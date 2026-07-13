package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.CalledAnalysis;

public interface AnalyzeCalledUseCase {
    CalledAnalysis analyze(String jiraKey);
}
