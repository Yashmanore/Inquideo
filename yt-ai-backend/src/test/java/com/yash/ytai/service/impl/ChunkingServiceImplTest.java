package com.yash.ytai.service.impl;

import com.yash.ytai.model.TranscriptChunk;
import com.yash.ytai.model.TranscriptItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ChunkingServiceImpl — verifies exact parity with Node.js chunkTranscriptWithOverlap().
 */
class ChunkingServiceImplTest {

    private ChunkingServiceImpl chunkingService;

    private static final long WINDOW_MS = 30_000L;
    private static final long OVERLAP_MS = 5_000L;

    @BeforeEach
    void setUp() {
        chunkingService = new ChunkingServiceImpl(WINDOW_MS, OVERLAP_MS);
    }

    @Test
    void chunk_emptyTranscript_returnsEmpty() {
        List<TranscriptChunk> result = chunkingService.chunk(List.of());
        assertThat(result).isEmpty();
    }

    @Test
    void chunk_singleItemTranscript_producesOneChunk() {
        List<TranscriptItem> transcript = List.of(
                TranscriptItem.builder().text("Hello world").offset(0L).duration(2000L).build()
        );
        List<TranscriptChunk> chunks = chunkingService.chunk(transcript);
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).getText()).isEqualTo("Hello world");
        assertThat(chunks.get(0).getStartTimeSec()).isEqualTo(0.0);
    }

    @Test
    void chunk_filtersAnnotations() {
        // Items starting with '[' or ending with ']' should be filtered out (exact from Node.js)
        List<TranscriptItem> transcript = List.of(
                TranscriptItem.builder().text("[Music]").offset(0L).duration(2000L).build(),
                TranscriptItem.builder().text("Hello world").offset(2000L).duration(2000L).build(),
                TranscriptItem.builder().text("[Applause]").offset(4000L).duration(1000L).build()
        );
        List<TranscriptChunk> chunks = chunkingService.chunk(transcript);
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).getText()).isEqualTo("Hello world");
    }

    @Test
    void chunk_slidingWindow_overlapPreservesItems() {
        // Create transcript spanning 40 seconds — should produce 2 chunks with overlap
        List<TranscriptItem> transcript = Arrays.asList(
                TranscriptItem.builder().text("A").offset(0L).duration(5000L).build(),
                TranscriptItem.builder().text("B").offset(10000L).duration(5000L).build(),
                TranscriptItem.builder().text("C").offset(20000L).duration(5000L).build(),
                TranscriptItem.builder().text("D").offset(27000L).duration(5000L).build(),   // in overlap (27s < 30s)
                TranscriptItem.builder().text("E").offset(35000L).duration(5000L).build()    // in 2nd window
        );
        List<TranscriptChunk> chunks = chunkingService.chunk(transcript);
        // Window 1: 0-30s covers A,B,C,D
        // Window 2: 25-55s covers D,E (D is in overlap)
        assertThat(chunks.size()).isGreaterThanOrEqualTo(2);

        // First chunk should contain A, B, C, D
        assertThat(chunks.get(0).getText()).contains("A").contains("B").contains("C").contains("D");
    }

    @Test
    void chunk_timestampsAreInSeconds() {
        List<TranscriptItem> transcript = List.of(
                TranscriptItem.builder().text("Hello").offset(5000L).duration(2000L).build()
        );
        List<TranscriptChunk> chunks = chunkingService.chunk(transcript);
        // startTimeSec should be offset/1000 = 5.0
        assertThat(chunks.get(0).getStartTimeSec()).isEqualTo(5.0);
    }

    @Test
    void chunk_nullTranscript_returnsEmpty() {
        List<TranscriptChunk> result = chunkingService.chunk(null);
        assertThat(result).isEmpty();
    }
}
