package com.yash.ytai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for {@code POST /api/v1/chat}.
 *
 * <p>Carries the session identifier (returned from video processing) and the user's question.
 */
@Data
@Schema(description = "Request body for asking a question about the processed video")
public class ChatRequest {

    @NotBlank(message = "sessionId must not be blank")
    @Schema(
            description = "Session ID returned from POST /api/v1/video/process",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String sessionId;

    @NotBlank(message = "question must not be blank")
    @Size(min = 2, max = 2000, message = "question must be between 2 and 2000 characters")
    @Schema(
            description = "The question to ask about the video",
            example = "What is the main topic discussed in this video?",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String question;
}
