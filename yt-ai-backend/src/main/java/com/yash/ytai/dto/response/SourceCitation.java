package com.yash.ytai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single source citation included in a chat response.
 *
 * <p>Corresponds to a Pinecone match result with timestamp metadata.
 * The Node.js equivalent builds this as:
 * <pre>
 * `[${formatTime(match.metadata.startTime)} - ${formatTime(match.metadata.endTime)}]\n${match.metadata.text}`
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A transcript chunk used as source context for the answer, with timestamp citation")
public class SourceCitation {

    @Schema(description = "Formatted start timestamp (M:SS)", example = "4:35")
    private String startTime;

    @Schema(description = "Formatted end timestamp (M:SS)", example = "5:05")
    private String endTime;

    @Schema(description = "The raw transcript text from this time range")
    private String text;

    @Schema(description = "Cosine similarity score from Pinecone", example = "0.87")
    private float score;
}
