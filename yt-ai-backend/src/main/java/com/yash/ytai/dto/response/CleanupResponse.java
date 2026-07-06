package com.yash.ytai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for {@code DELETE /api/v1/cleanup/{sessionId}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of a session cleanup operation")
public class CleanupResponse {

    @Schema(description = "The session ID that was cleaned up")
    private String sessionId;

    @Schema(description = "Human-readable status message",
            example = "Session cleaned up successfully. Pinecone namespace cleared.")
    private String message;
}
