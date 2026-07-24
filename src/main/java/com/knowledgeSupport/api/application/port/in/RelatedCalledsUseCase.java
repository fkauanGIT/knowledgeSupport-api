package com.knowledgeSupport.api.application.port.in;

import com.knowledgeSupport.api.domain.model.CalledMatch;

import java.util.List;
import java.util.UUID;

/**
 * Given a document, finds which open Jira tickets it likely resolves — scores every open
 * ticket's title+errorName against the document's own chunks (TF-IDF).
 */
public interface RelatedCalledsUseCase {
    /** @throws java.util.NoSuchElementException if no document exists with that id */
    List<CalledMatch> relatedCalleds(UUID documentId);
}
