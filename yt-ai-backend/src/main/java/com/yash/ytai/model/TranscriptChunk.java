package com.yash.ytai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A time-bounded chunk of transcript text produced by the sliding window chunker.
 *
 * <p>Maps to a LangChain {@code Document} in the original Node.js implementation:
 * <pre>
 * new Document({
 *     pageContent: cleanedTextArray.join(' '),
 *     metadata: { startTime: chunkStartMs/1000, endTime: min(chunkEndMs,videoEndMs)/1000 }
 * })
 * </pre>
 *
 * <p>Timestamps are stored as <strong>seconds</strong> (float), matching {@code metadata.startTime}
 * and {@code metadata.endTime} stored in Pinecone.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptChunk {

    /** The concatenated, cleaned text for this chunk. */
    private String text;

    /** Start time in seconds (e.g. 275.0 for 4 minutes 35 seconds). */
    private double startTimeSec;

    /** End time in seconds. */
    private double endTimeSec;

    /** The 768-dimensional embedding vector (populated by EmbeddingService). */
    private List<Float> embedding;
}
