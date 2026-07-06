package com.yash.ytai.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yash.ytai.config.GeminiConfig;
import com.yash.ytai.exception.EmbeddingException;
import com.yash.ytai.service.EmbeddingService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calls the Gemini {@code gemini-embedding-001} REST API to generate 768-dimensional embeddings.
 *
 * <p>REST endpoint used:
 * <pre>
 * POST https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent
 * </pre>
 *
 * <p>Request body mirrors the Node.js {@code ai.models.embedContent()} call:
 * <pre>
 * {
 *   "model": "models/gemini-embedding-001",
 *   "content": { "parts": [{ "text": "..." }] },
 *   "taskType": "RETRIEVAL_DOCUMENT",
 *   "outputDimensionality": 768
 * }
 * </pre>
 */
@Service
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final String EMBED_PATH = "/v1beta/models/{model}:embedContent";

    private final WebClient geminiWebClient;
    private final GeminiConfig geminiConfig;

    public EmbeddingServiceImpl(
            @Qualifier("geminiWebClient") WebClient geminiWebClient,
            GeminiConfig geminiConfig) {
        this.geminiWebClient = geminiWebClient;
        this.geminiConfig = geminiConfig;
    }

    @Override
    public List<Float> embedDocument(String text) {
        return embed(text, "RETRIEVAL_DOCUMENT");
    }

    @Override
    public List<Float> embedQuery(String text) {
        return embed(text, "RETRIEVAL_QUERY");
    }

    /**
     * Shared embedding logic. Builds the request body and calls the Gemini embedContent API.
     */
    private List<Float> embed(String text, String taskType) {
        log.debug("Embedding text ({}) with taskType={}, length={}",
                taskType, taskType, text.length());

        // Build request body matching Gemini REST API spec
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "models/" + geminiConfig.getEmbeddingModel());
        requestBody.put("content", Map.of(
                "parts", List.of(Map.of("text", text))
        ));
        requestBody.put("taskType", taskType);
        requestBody.put("outputDimensionality", geminiConfig.getOutputDimensionality());

        try {
            EmbedContentResponse response = geminiWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(EMBED_PATH)
                            .build(geminiConfig.getEmbeddingModel()))
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(EmbedContentResponse.class)
                    .block();

            if (response == null || response.getEmbedding() == null
                    || response.getEmbedding().getValues() == null) {
                throw new EmbeddingException("Empty embedding response from Gemini API");
            }

            return response.getEmbedding().getValues();

        } catch (WebClientResponseException e) {
            throw new EmbeddingException(
                    "Gemini embedding API error [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e);
        } catch (EmbeddingException e) {
            throw e;
        } catch (Exception e) {
            throw new EmbeddingException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }

    // ── Response deserialization ──────────────────────────────────────────────

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EmbedContentResponse {
        private EmbeddingValues embedding;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EmbeddingValues {
        private List<Float> values;
    }
}
