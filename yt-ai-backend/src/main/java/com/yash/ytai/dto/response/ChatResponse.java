package com.yash.ytai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for {@code POST /api/v1/chat}.
 *
 * <p>Returns the Gemini-generated answer along with the source transcript chunks
 * used as context, so the frontend can render timestamp citations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response from a chat query — includes the answer and source citations")
public class ChatResponse {

    @Schema(description = "The session ID for this conversation")
    private String sessionId;

    @Schema(description = "Gemini-generated answer with timestamp citations embedded")
    private String answer;

    @Schema(description = "The top-5 transcript chunks retrieved from Pinecone, used as context")
    private List<SourceCitation> sources;
}
