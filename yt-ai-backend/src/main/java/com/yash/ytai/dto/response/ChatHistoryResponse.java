package com.yash.ytai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for {@code GET /api/v1/chat/history/{sessionId}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full conversation history for a session")
public class ChatHistoryResponse {

    @Schema(description = "The session ID")
    private String sessionId;

    @Schema(description = "Ordered list of messages in this conversation")
    private List<MessageEntry> messages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "A single message in the conversation history")
    public static class MessageEntry {

        @Schema(description = "Role of the sender", example = "user",
                allowableValues = {"user", "model"})
        private String role;

        @Schema(description = "Message text content")
        private String text;
    }
}
