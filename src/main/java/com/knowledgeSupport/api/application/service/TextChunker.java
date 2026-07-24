package com.knowledgeSupport.api.application.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a page/file's text into ~700-char blocks without cutting a word in half — looks
 * for the last space before the limit and breaks there instead.
 */
public final class TextChunker {

    public static final int DEFAULT_MAX_LENGTH = 700;

    private TextChunker() {}

    public static List<String> chunk(String text) {
        return chunk(text, DEFAULT_MAX_LENGTH);
    }

    public static List<String> chunk(String text, int maxLength) {
        List<String> chunks = new ArrayList<>();
        if (text == null) {
            return chunks;
        }
        String remaining = text.strip();
        while (!remaining.isEmpty()) {
            if (remaining.length() <= maxLength) {
                chunks.add(remaining);
                break;
            }
            int cut = remaining.lastIndexOf(' ', maxLength);
            if (cut <= 0) {
                cut = maxLength; // no space in range — hard cut as a last resort
            }
            String piece = remaining.substring(0, cut).strip();
            if (!piece.isEmpty()) {
                chunks.add(piece);
            }
            remaining = remaining.substring(cut).strip();
        }
        return chunks;
    }
}
