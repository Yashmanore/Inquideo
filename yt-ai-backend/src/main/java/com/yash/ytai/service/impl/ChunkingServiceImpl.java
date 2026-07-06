package com.yash.ytai.service.impl;

import com.yash.ytai.model.TranscriptChunk;
import com.yash.ytai.model.TranscriptItem;
import com.yash.ytai.service.ChunkingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sliding window transcript chunker — exact Java port of {@code chunkTranscriptWithOverlap()}
 * from {@code index.js}.
 *
 * <p>Algorithm (from Node.js, preserved exactly):
 * <pre>
 * const CHUNK_WINDOW_MS = 30000;  // 30s
 * const OVERLAP_MS      = 5000;   // 5s
 * const STEP_MS         = 25000;  // window - overlap
 *
 * let chunkStartTime = rawTranscript[0].offset;
 * while (chunkStartTime < videoEndMs) {
 *     const chunkEndTime = chunkStartTime + CHUNK_WINDOW_MS;
 *     const itemsInWindow = rawTranscript.filter(item =>
 *         item.offset >= chunkStartTime && item.offset < chunkEndTime
 *     );
 *     // filter [annotations], join with space, create Document
 *     chunkStartTime += STEP_MS;
 * }
 * </pre>
 */
@Service
@Slf4j
public class ChunkingServiceImpl implements ChunkingService {

    private final long chunkWindowMs;
    private final long stepMs;

    public ChunkingServiceImpl(
            @Value("${rag.chunking.window-ms}") long chunkWindowMs,
            @Value("${rag.chunking.overlap-ms}") long overlapMs) {
        this.chunkWindowMs = chunkWindowMs;
        this.stepMs = chunkWindowMs - overlapMs;  // step = window - overlap (25000ms)
    }

    @Override
    public List<TranscriptChunk> chunk(List<TranscriptItem> transcript) {
        if (transcript == null || transcript.isEmpty()) {
            log.warn("Received empty transcript — returning empty chunk list");
            return List.of();
        }

        List<TranscriptChunk> documents = new ArrayList<>();

        // Video end time = offset of last item + its duration (exact from Node.js)
        TranscriptItem lastItem = transcript.get(transcript.size() - 1);
        long videoEndMs = lastItem.getOffset() + lastItem.getDuration();

        long chunkStartTime = transcript.get(0).getOffset();

        while (chunkStartTime < videoEndMs) {
            // Capture as effectively-final locals for use in lambda expressions
            final long windowStart = chunkStartTime;
            final long windowEnd   = chunkStartTime + chunkWindowMs;

            // Collect items in this window (exact filter from Node.js)
            List<TranscriptItem> itemsInWindow = transcript.stream()
                    .filter(item -> item.getOffset() >= windowStart
                            && item.getOffset() < windowEnd)
                    .collect(Collectors.toList());

            // Filter out [annotation] items — exact port of Node.js:
            // .filter(item => !item.text.startsWith('[') && !item.text.endsWith(']'))
            String joinedText = itemsInWindow.stream()
                    .filter(item -> !item.getText().startsWith("[")
                            && !item.getText().endsWith("]"))
                    .map(item -> item.getText().trim())
                    .filter(text -> !text.isBlank())
                    .collect(Collectors.joining(" "));

            if (!joinedText.isBlank()) {
                documents.add(TranscriptChunk.builder()
                        .text(joinedText)
                        .startTimeSec((double) windowStart / 1000.0)          // ms → seconds
                        .endTimeSec((double) Math.min(windowEnd, videoEndMs) / 1000.0)
                        .build());
            }

            chunkStartTime += stepMs;
        }

        log.info("Chunking complete: {} chunks produced from {} transcript items",
                documents.size(), transcript.size());
        return documents;
    }
}
