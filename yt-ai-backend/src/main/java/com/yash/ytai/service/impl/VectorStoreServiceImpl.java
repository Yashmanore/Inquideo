package com.yash.ytai.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yash.ytai.exception.VectorStoreException;
import com.yash.ytai.model.TranscriptChunk;
import com.yash.ytai.service.VectorStoreService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages vector storage and retrieval in Pinecone via REST API.
 *
 * <p>Pinecone REST endpoints used:
 * <ul>
 *   <li>Upsert: {@code POST /vectors/upsert}</li>
 *   <li>Query: {@code POST /query}</li>
 *   <li>Delete all: {@code POST /vectors/delete} with {@code deleteAll: true}</li>
 * </ul>
 *
 * <p>Namespace is the {@code sessionId}, enabling concurrent user sessions
 * with isolated vector spaces.
 */
@Service
@Slf4j
public class VectorStoreServiceImpl implements VectorStoreService {

    private final WebClient pineconeWebClient;
    private final int batchSize;

    public VectorStoreServiceImpl(
            @Qualifier("pineconeWebClient") WebClient pineconeWebClient,
            @Value("${pinecone.upsert.batch-size}") int batchSize) {
        this.pineconeWebClient = pineconeWebClient;
        this.batchSize = batchSize;
    }

    @Override
    public void upsertChunks(List<TranscriptChunk> chunks, String namespace) {
        // Filter empty chunks (mirrors Node.js: validChunks filter)
        List<TranscriptChunk> validChunks = chunks.stream()
                .filter(c -> c.getText() != null && !c.getText().isBlank()
                        && c.getEmbedding() != null && !c.getEmbedding().isEmpty())
                .collect(Collectors.toList());

        if (validChunks.isEmpty()) {
            log.warn("No valid chunks to upsert for namespace: {}", namespace);
            return;
        }

        log.info("Upserting {} chunks to Pinecone namespace: {}", validChunks.size(), namespace);

        // Batch upsert (mirrors Node.js maxConcurrency: 2 via sequential batches)
        List<List<TranscriptChunk>> batches = partition(validChunks, batchSize);

        for (List<TranscriptChunk> batch : batches) {
            upsertBatch(batch, namespace);
        }

        log.info("Pinecone upsert complete for namespace: {}", namespace);
    }

    private void upsertBatch(List<TranscriptChunk> batch, String namespace) {
        List<Map<String, Object>> vectors = batch.stream().map(chunk -> {
            Map<String, Object> vector = new HashMap<>();
            vector.put("id", UUID.randomUUID().toString());
            vector.put("values", chunk.getEmbedding());
            vector.put("metadata", Map.of(
                    "text", chunk.getText(),
                    "startTime", chunk.getStartTimeSec(),
                    "endTime", chunk.getEndTimeSec()
            ));
            return vector;
        }).collect(Collectors.toList());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vectors", vectors);
        requestBody.put("namespace", namespace);

        try {
            pineconeWebClient.post()
                    .uri("/vectors/upsert")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new VectorStoreException(
                    "Pinecone upsert failed [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new VectorStoreException("Pinecone upsert failed: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> similaritySearch(
            List<Float> queryVector, int topK, String namespace) {

        log.debug("Querying Pinecone: topK={}, namespace={}", topK, namespace);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vector", queryVector);
        requestBody.put("topK", topK);
        requestBody.put("includeMetadata", true);    // exact match: Node.js includeMetadata: true
        requestBody.put("namespace", namespace);

        try {
            PineconeQueryResponse response = pineconeWebClient.post()
                    .uri("/query")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(PineconeQueryResponse.class)
                    .block();

            if (response == null || response.getMatches() == null) {
                return List.of();
            }

            return response.getMatches();

        } catch (WebClientResponseException e) {
            throw new VectorStoreException(
                    "Pinecone query failed [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new VectorStoreException("Pinecone query failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAllInNamespace(String namespace) {
        log.info("Deleting all vectors in Pinecone namespace: '{}'", namespace);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("deleteAll", true);
        requestBody.put("namespace", namespace);

        try {
            pineconeWebClient.post()
                    .uri("/vectors/delete")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("✅ All vectors deleted from namespace: '{}'", namespace);

        } catch (WebClientResponseException e) {
            throw new VectorStoreException(
                    "Pinecone delete failed [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new VectorStoreException("Pinecone delete failed: " + e.getMessage(), e);
        }
    }

    /** Partitions a list into fixed-size sublists. */
    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    // ── Response deserialization ──────────────────────────────────────────────

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PineconeQueryResponse {
        private List<Map<String, Object>> matches;
    }
}
