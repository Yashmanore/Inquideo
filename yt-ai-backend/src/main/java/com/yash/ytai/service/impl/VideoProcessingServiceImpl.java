package com.yash.ytai.service.impl;

import com.yash.ytai.dto.response.ProcessVideoResponse;
import com.yash.ytai.model.TranscriptChunk;
import com.yash.ytai.model.TranscriptItem;
import com.yash.ytai.service.*;
import com.yash.ytai.util.VideoIdExtractorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Orchestrates the full video ingestion pipeline.
 *
 * <p>Exact port of the Node.js flow: {@code loadTranscript()} + {@code convertIntoVector()}.
 *
 * <p>Order of operations:
 * <ol>
 *   <li>Extract 11-char video ID from URL</li>
 *   <li>Fetch transcript via YouTube timedtext scraping</li>
 *   <li>Chunk with sliding window (30s / 5s overlap)</li>
 *   <li>Embed each valid chunk (RETRIEVAL_DOCUMENT, 768-dim)</li>
 *   <li>Upsert embedded chunks to Pinecone (session namespace)</li>
 *   <li>Initialize conversation session in memory</li>
 *   <li>Return session ID + stats</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingServiceImpl implements VideoProcessingService {

    private final TranscriptService transcriptService;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final ConversationMemoryService memoryService;

    @Override
    public ProcessVideoResponse processVideo(String videoUrl) {
        // Step 1: Extract video ID
        String videoId = VideoIdExtractorUtil.extract(videoUrl);
        log.info("Processing video: {}", videoId);

        // Step 2: Fetch transcript
        List<TranscriptItem> transcript = transcriptService.fetchTranscript(videoId);
        log.info("Fetched {} transcript items", transcript.size());

        // Step 3: Chunk
        List<TranscriptChunk> chunks = chunkingService.chunk(transcript);
        log.info("Produced {} chunks", chunks.size());

        // Step 4: Embed chunks in batch (filters empty ones first)
        List<TranscriptChunk> validChunks = chunks.stream()
                .filter(c -> c.getText() != null && !c.getText().isBlank())
                .collect(Collectors.toList());

        List<String> texts = validChunks.stream()
                .map(TranscriptChunk::getText)
                .collect(Collectors.toList());

        List<List<Float>> embeddings = embeddingService.embedDocuments(texts);

        for (int i = 0; i < validChunks.size(); i++) {
            validChunks.get(i).setEmbedding(embeddings.get(i));
        }

        List<TranscriptChunk> embeddedChunks = validChunks;

        log.info("Generated embeddings for {} chunks in batch", embeddedChunks.size());

        // Step 5: Generate session ID and upsert to Pinecone
        String sessionId = UUID.randomUUID().toString();
        vectorStoreService.upsertChunks(embeddedChunks, sessionId);

        // Step 6: Initialize conversation memory for this session
        memoryService.initSession(sessionId);

        log.info("Video processing complete — sessionId={}, chunksProcessed={}",
                sessionId, embeddedChunks.size());

        return ProcessVideoResponse.builder()
                .sessionId(sessionId)
                .videoId(videoId)
                .chunksProcessed(embeddedChunks.size())
                .message("Video processed successfully. " + embeddedChunks.size()
                        + " chunks are ready for querying.")
                .build();
    }
}
