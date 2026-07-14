package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.in.AnalyzeCalledUseCase;
import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.application.port.out.StandardRepositoryPort;
import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.CalledAnalysis;
import com.knowledgeSupport.api.domain.model.Standard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AnalyzeCalledService implements AnalyzeCalledUseCase {

    private final CalledProviderPort calledProviderPort;
    private final StandardRepositoryPort standardRepositoryPort;
    private final CalledStandardMatcher matcher;

    public AnalyzeCalledService(CalledProviderPort calledProviderPort,
                                 StandardRepositoryPort standardRepositoryPort,
                                 @Value("${matching.threshold:0.4}") double threshold,
                                 @Value("${matching.high-confidence-threshold:0.75}") double highConfidenceThreshold) {
        this.calledProviderPort = calledProviderPort;
        this.standardRepositoryPort = standardRepositoryPort;
        this.matcher = new CalledStandardMatcher(threshold, highConfidenceThreshold);
    }

    @Override
    public CalledAnalysis analyze(String jiraKey) {
        Called called = calledProviderPort.fetchByKey(jiraKey)
                .orElseThrow(() -> new NoSuchElementException("Called not found: " + jiraKey));

        List<Standard> standards = standardRepositoryPort.findAll();

        return matcher.match(called, standards);
    }
}
