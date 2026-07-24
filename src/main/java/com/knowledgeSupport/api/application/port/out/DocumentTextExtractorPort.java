package com.knowledgeSupport.api.application.port.out;

import com.knowledgeSupport.api.domain.model.enums.DocumentType;

import java.util.List;

/**
 * Outbound port: pulls raw text out of a file, one entry per page (PDF) or a single entry
 * for the whole file (DOCX — no real pagination in the XML).
 */
public interface DocumentTextExtractorPort {
    /** @throws DocumentExtractionException if the file can't be parsed at all */
    List<ExtractedPage> extract(byte[] content, DocumentType type);
}
