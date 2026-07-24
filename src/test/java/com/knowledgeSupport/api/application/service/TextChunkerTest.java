package com.knowledgeSupport.api.application.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextChunkerTest {

    @Test
    void chunk_textShorterThanLimit_returnsSingleChunk() {
        List<String> chunks = TextChunker.chunk("texto curto", 700);

        assertEquals(List.of("texto curto"), chunks);
    }

    @Test
    void chunk_neverSplitsAWordInHalf() {
        String word = "a".repeat(10);
        // Five 10-char words separated by spaces, limit forces a break mid-sentence.
        String text = String.join(" ", List.of(word, word, word, word, word));

        List<String> chunks = TextChunker.chunk(text, 25);

        for (String chunk : chunks) {
            assertTrue(text.contains(chunk));
            // Every chunk boundary lands on a full word — no fragment like "aaaaa" (half of "aaaaaaaaaa").
            for (String piece : chunk.split(" ")) {
                assertEquals(word, piece);
            }
        }
        assertEquals(text.replace(" ", ""), String.join("", chunks).replace(" ", ""));
    }

    @Test
    void chunk_nullText_returnsEmptyList() {
        assertTrue(TextChunker.chunk(null, 700).isEmpty());
    }

    @Test
    void chunk_blankText_returnsEmptyList() {
        assertTrue(TextChunker.chunk("   ", 700).isEmpty());
    }

    @Test
    void chunk_singleWordLongerThanLimit_hardCutsAsLastResort() {
        String longWord = "x".repeat(50);

        List<String> chunks = TextChunker.chunk(longWord, 20);

        assertEquals(3, chunks.size());
        assertEquals(longWord, String.join("", chunks));
    }
}
