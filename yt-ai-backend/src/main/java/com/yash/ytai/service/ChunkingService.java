package com.yash.ytai.service;

import com.yash.ytai.model.TranscriptChunk;
import com.yash.ytai.model.TranscriptItem;

import java.util.List;

/**
 * Splits a raw transcript into overlapping time-based chunks.
 */
public interface ChunkingService {

    /**
     * Applies a sliding window chunking algorithm to the raw transcript.
     *
     * <p>Constants (exact match to Node.js):
     * <ul>
     *   <li>Window: 30 seconds</li>
     *   <li>Overlap: 5 seconds</li>
     *   <li>Step: 25 seconds</li>
     * </ul>
     *
     * @param transcript the ordered list of raw transcript items
     * @return list of {@link TranscriptChunk} objects with text and timestamps (seconds)
     */
    List<TranscriptChunk> chunk(List<TranscriptItem> transcript);
}
