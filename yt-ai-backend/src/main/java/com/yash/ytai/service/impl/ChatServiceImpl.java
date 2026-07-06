package com.yash.ytai.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yash.ytai.config.GeminiConfig;
import com.yash.ytai.config.PineconeConfig;
import com.yash.ytai.dto.response.ChatResponse;
import com.yash.ytai.dto.response.SourceCitation;
import com.yash.ytai.exception.EmbeddingException;
import com.yash.ytai.exception.SessionNotFoundException;
import com.yash.ytai.model.ChatMessage;
import com.yash.ytai.service.ChatService;
import com.yash.ytai.service.ConversationMemoryService;
import com.yash.ytai.service.EmbeddingService;
import com.yash.ytai.service.VectorStoreService;
import com.yash.ytai.util.TimeFormatterUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Full RAG chat turn implementation — exact port of {@code chatting(question)} from {@code query.js}.
 *
 * <p>Pipeline per turn:
 * <ol>
 *   <li>Embed question (RETRIEVAL_QUERY, 768-dim)</li>
 *   <li>Query Pinecone topK=5 in session namespace</li>
 *   <li>Build context string with formatted timestamps ({@code [M:SS - M:SS]\n<text>})</li>
 *   <li>Push user turn to {@link ConversationMemoryService}</li>
 *   <li>Call Gemini 2.5 Flash with full history + system prompt (containing context)</li>
 *   <li>Push model turn to history</li>
 *   <li>Return answer + source citations</li>
 * </ol>
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private static final String GENERATE_PATH = "/v1beta/models/{model}:generateContent";

    /**
     * System prompt — exact copy from {@code query.js}.
     */
    private static final String SYSTEM_INSTRUCTION = """
            You have to behave like a helpful assistant and answer the questions
            based on the context you have got. You are a helpful assistant which takes input from the user about a YouTube video link.
            You get context regarding the question asked.
            When answering, cite the timestamps in [] format to show where in the video the information comes from.
            If the answer is not in your context, let the user know in polite manner.
            Context:
            """;

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final ConversationMemoryService memoryService;
    private final GeminiConfig geminiConfig;
    private final PineconeConfig pineconeConfig;
    private final WebClient geminiWebClient;

    public ChatServiceImpl(
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService,
            ConversationMemoryService memoryService,
            GeminiConfig geminiConfig,
            PineconeConfig pineconeConfig,
            @Qualifier("geminiWebClient") WebClient geminiWebClient) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.memoryService = memoryService;
        this.geminiConfig = geminiConfig;
        this.pineconeConfig = pineconeConfig;
        this.geminiWebClient = geminiWebClient;
    }

    @Override
    public ChatResponse chat(String sessionId, String question) {
        log.info("Chat turn — sessionId={}, question length={}", sessionId, question.length());

        // Validate session exists
        if (!memoryService.sessionExists(sessionId)) {
            throw new SessionNotFoundException(sessionId);
        }

        // Step 1: Embed the question (RETRIEVAL_QUERY)
        List<Float> queryEmbedding = embeddingService.embedQuery(question);

        // Step 2: Query Pinecone (topK=5, exact from Node.js)
        List<Map<String, Object>> matches = vectorStoreService.similaritySearch(
                queryEmbedding, pineconeConfig.getTopK(), sessionId);

        // Step 3: Build source citations + context string
        List<SourceCitation> sources = buildSourceCitations(matches);
        String context = buildContextString(sources);

        // Step 4: Push user turn to history
        memoryService.addMessage(sessionId, "user", question);

        // Step 5: Build Gemini request with full history + system instruction
        String answer = callGemini(sessionId, context);

        // Step 6: Push model turn to history
        memoryService.addMessage(sessionId, "model", answer);

        log.info("Chat turn complete — sessionId={}, answer length={}", sessionId, answer.length());

        return ChatResponse.builder()
                .sessionId(sessionId)
                .answer(answer)
                .sources(sources)
                .build();
    }

    /**
     * Builds {@link SourceCitation} list from Pinecone matches.
     * Formats timestamps using {@link TimeFormatterUtil} — exact port of Node.js formatTime().
     */
    @SuppressWarnings("unchecked")
    private List<SourceCitation> buildSourceCitations(List<Map<String, Object>> matches) {
        return matches.stream().map(match -> {
            Map<String, Object> metadata = (Map<String, Object>) match.getOrDefault("metadata", Map.of());

            double startTimeSec = toDouble(metadata.getOrDefault("startTime", 0.0));
            double endTimeSec   = toDouble(metadata.getOrDefault("endTime", 0.0));
            String text         = (String) metadata.getOrDefault("text", "");
            float score         = ((Number) match.getOrDefault("score", 0.0)).floatValue();

            return SourceCitation.builder()
                    .startTime(TimeFormatterUtil.format(startTimeSec))
                    .endTime(TimeFormatterUtil.format(endTimeSec))
                    .text(text)
                    .score(score)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Builds the context string injected into the system prompt.
     *
     * <p>Exact format from Node.js:
     * <pre>
     * [4:35 - 5:05]\ntext\n\n---\n\n[M:SS - M:SS]\ntext
     * </pre>
     */
    private String buildContextString(List<SourceCitation> sources) {
        return sources.stream()
                .map(s -> "[" + s.getStartTime() + " - " + s.getEndTime() + "]\n" + s.getText())
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    /**
     * Calls Gemini 2.5 Flash with the full conversation history and context-augmented system prompt.
     * Exact port of {@code ai.models.generateContent({ model, contents: History, config: { systemInstruction } })}.
     */
    @SuppressWarnings("unchecked")
    private String callGemini(String sessionId, String context) {
        List<ChatMessage> history = memoryService.getHistory(sessionId);

        // Build contents array (History in Node.js format: [{role, parts: [{text}]}])
        List<Map<String, Object>> contents = history.stream()
                .map(msg -> Map.<String, Object>of(
                        "role", msg.getRole(),
                        "parts", List.of(Map.of("text", msg.getText()))
                ))
                .collect(Collectors.toList());

        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", contents);
        requestBody.put("systemInstruction", Map.of(
                "parts", List.of(Map.of("text", SYSTEM_INSTRUCTION + context))
        ));

        try {
            Map<String, Object> response = geminiWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(GENERATE_PATH)
                            .build(geminiConfig.getGenerationModel()))
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new EmbeddingException("Empty response from Gemini generateContent API");
            }

            // Extract text from: candidates[0].content.parts[0].text
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "I'm unable to generate a response at this time.";
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");

        } catch (WebClientResponseException e) {
            throw new EmbeddingException(
                    "Gemini generateContent API error [" + e.getStatusCode() + "]: "
                    + e.getResponseBodyAsString(), e);
        } catch (EmbeddingException e) {
            throw e;
        } catch (Exception e) {
            throw new EmbeddingException("Gemini generation failed: " + e.getMessage(), e);
        }
    }

    private double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (Exception e) { return 0.0; }
    }
}
