package com.knowledgeSupport.api.application.service;

import com.knowledgeSupport.api.application.port.in.ListCalledsUseCase;
import com.knowledgeSupport.api.application.port.out.CalledProviderPort;
import com.knowledgeSupport.api.domain.model.Called;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Núcleo: por enquanto só repassa, mas é AQUI que vai morar a regra de negócio
 * central do produto — comparar o errorName/incidentType do Called com os
 * Standards para sugerir a solução padrão (futuro AnalyzeCalledUseCase).
 * Repare: nenhum import de HTTP, Jira ou banco. Só ports e domínio.
 */
@Service
public class CalledService implements ListCalledsUseCase {

    private final CalledProviderPort calledProviderPort;

    public CalledService(CalledProviderPort calledProviderPort) {
        this.calledProviderPort = calledProviderPort;
    }

    @Override
    public List<Called> listOpenCalleds() {
        return calledProviderPort.fetchOpenCalleds();
    }
}
