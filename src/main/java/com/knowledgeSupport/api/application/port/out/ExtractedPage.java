package com.knowledgeSupport.api.application.port.out;

/**
 * One page's raw text as pulled from the source file, before chunking. {@code page} is
 * 1-based and null when the format has no real pagination (DOCX).
 */
public record ExtractedPage(Integer page, String text) {
}
