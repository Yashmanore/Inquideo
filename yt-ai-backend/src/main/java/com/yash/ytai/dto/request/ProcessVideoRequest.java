package com.yash.ytai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request DTO for {@code POST /api/v1/video/process}.
 *
 * <p>Accepts either a full YouTube URL or a bare 11-character video ID:
 * <ul>
 *   <li>{@code https://www.youtube.com/watch?v=dQw4w9WgXcQ}</li>
 *   <li>{@code https://youtu.be/dQw4w9WgXcQ}</li>
 *   <li>{@code dQw4w9WgXcQ}</li>
 * </ul>
 */
@Data
@Schema(description = "Request body for processing a YouTube video")
public class ProcessVideoRequest {

    @NotBlank(message = "videoUrl must not be blank")
    @Pattern(
            regexp = "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)?[a-zA-Z0-9_-]{11}.*$",
            message = "Must be a valid YouTube URL or 11-character video ID"
    )
    @Schema(
            description = "YouTube video URL or bare video ID",
            example = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String videoUrl;
}
