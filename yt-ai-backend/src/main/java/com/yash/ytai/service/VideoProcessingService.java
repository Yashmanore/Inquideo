package com.yash.ytai.service;

import com.yash.ytai.dto.response.ProcessVideoResponse;

/**
 * Orchestrates the full video ingestion pipeline.
 *
 * <p>Maps to the Node.js {@code loadTranscript()} + {@code convertIntoVector()} flow:
 * <ol>
 *   <li>Extract video ID from URL</li>
 *   <li>Fetch transcript</li>
 *   <li>Chunk with sliding window</li>
 *   <li>Embed each chunk (RETRIEVAL_DOCUMENT)</li>
 *   <li>Upsert to Pinecone</li>
 *   <li>Initialize conversation session</li>
 * </ol>
 */
public interface VideoProcessingService {

    /**
     * Processes a YouTube video and returns a session for chatting.
     *
     * @param videoUrl the YouTube URL or bare video ID entered by the user
     * @return {@link ProcessVideoResponse} containing the session ID and processing stats
     */
    ProcessVideoResponse processVideo(String videoUrl);
}
