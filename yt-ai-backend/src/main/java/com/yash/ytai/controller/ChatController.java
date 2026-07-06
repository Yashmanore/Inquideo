package com.yash.ytai.controller;

import com.yash.ytai.dto.request.ChatRequest;
import com.yash.ytai.dto.response.ChatHistoryResponse;
import com.yash.ytai.dto.response.ChatResponse;
import com.yash.ytai.model.ChatMessage;
import com.yash.ytai.service.ChatService;
import com.yash.ytai.service.ConversationMemoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for multi-turn RAG chat.
 *
 * <p>Exposes two endpoints:
 * <ul>
 *   <li>{@code POST /api/v1/chat} — ask a question about the processed video</li>
 *   <li>{@code GET /api/v1/chat/history/{sessionId}} — retrieve conversation history</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "Multi-turn RAG chat with YouTube video context")
public class ChatController {

    private final ChatService chatService;
    private final ConversationMemoryService memoryService;

    /**
     * POST /api/v1/chat
     *
     * <p>Executes one full RAG turn:
     * embed → retrieve → generate → history update → return answer + sources.
     */
    @PostMapping
    @Operation(
            summary = "Ask a question about the processed video",
            description = """
                    Embeds the question using Gemini (RETRIEVAL_QUERY, 768-dim),
                    retrieves the top-5 relevant transcript chunks from Pinecone,
                    builds context with timestamp citations, and generates an answer
                    using Gemini 2.5 Flash with the full conversation history.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Answer generated with source citations"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Session not found — process a video first"),
            @ApiResponse(responseCode = "502", description = "Gemini or Pinecone API error")
    })
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Chat request — sessionId={}", request.getSessionId());
        ChatResponse response = chatService.chat(request.getSessionId(), request.getQuestion());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/chat/history/{sessionId}
     *
     * <p>Returns the full ordered conversation history for the given session.
     */
    @GetMapping("/history/{sessionId}")
    @Operation(
            summary = "Get conversation history for a session",
            description = "Returns all user and model messages in the session, in order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History returned"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<ChatHistoryResponse> getHistory(
            @Parameter(description = "Session ID returned from video processing",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionId) {

        List<ChatMessage> history = memoryService.getHistory(sessionId);

        List<ChatHistoryResponse.MessageEntry> messages = history.stream()
                .map(msg -> ChatHistoryResponse.MessageEntry.builder()
                        .role(msg.getRole())
                        .text(msg.getText())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ChatHistoryResponse.builder()
                .sessionId(sessionId)
                .messages(messages)
                .build());
    }
}
