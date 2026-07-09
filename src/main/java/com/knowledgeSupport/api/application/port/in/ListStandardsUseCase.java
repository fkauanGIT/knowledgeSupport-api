package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.Standard;

import java.util.List;

public interface ListStandardsUseCase {
    List<Standard> listAll();
}
