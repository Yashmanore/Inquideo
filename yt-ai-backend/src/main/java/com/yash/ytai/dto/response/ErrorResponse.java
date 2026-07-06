package com.yash.ytai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response envelope returned by {@link com.yash.ytai.exception.GlobalExceptionHandler}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type / category", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Human-readable error message")
    private String message;

    @Schema(description = "API path that caused the error", example = "/api/v1/video/process")
    private String path;

    @Schema(description = "Timestamp of the error")
    private LocalDateTime timestamp;
}
