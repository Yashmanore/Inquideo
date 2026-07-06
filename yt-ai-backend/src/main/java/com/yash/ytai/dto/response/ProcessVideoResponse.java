package com.yash.ytai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for {@code POST /api/v1/video/process}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of processing a YouTube video")
public class ProcessVideoResponse {

    @Schema(description = "Unique session ID to use for subsequent chat requests",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    @Schema(description = "The extracted YouTube video ID", example = "dQw4w9WgXcQ")
    private String videoId;

    @Schema(description = "Number of transcript chunks embedded and stored in Pinecone",
            example = "42")
    private int chunksProcessed;

    @Schema(description = "Human-readable status message",
            example = "Video processed successfully. 42 chunks ready for querying.")
    private String message;
}
