package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.IndexedDocument;

import java.util.List;

public interface ListDocumentsUseCase {
    List<IndexedDocument> listAll();
}
