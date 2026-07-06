package com.yash.ytai.exception;

import com.yash.ytai.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for all controllers.
 *
 * <p>Maps each application exception to a structured {@link ErrorResponse}
 * so the frontend always receives a consistent JSON error envelope.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── Validation errors (Bean Validation) ──────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("Validation error on {}: {}", request.getRequestURI(), message);

        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    // ── Domain exceptions ────────────────────────────────────────────────────

    @ExceptionHandler(TranscriptFetchException.class)
    public ResponseEntity<ErrorResponse> handleTranscript(
            TranscriptFetchException ex, HttpServletRequest request) {

        log.error("Transcript fetch failed: {}", ex.getMessage());

        return ResponseEntity.unprocessableEntity().body(ErrorResponse.builder()
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("TRANSCRIPT_FETCH_ERROR")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(EmbeddingException.class)
    public ResponseEntity<ErrorResponse> handleEmbedding(
            EmbeddingException ex, HttpServletRequest request) {

        log.error("Embedding failed: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ErrorResponse.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("EMBEDDING_ERROR")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(VectorStoreException.class)
    public ResponseEntity<ErrorResponse> handleVectorStore(
            VectorStoreException ex, HttpServletRequest request) {

        log.error("Vector store operation failed: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ErrorResponse.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("VECTOR_STORE_ERROR")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotFound(
            SessionNotFoundException ex, HttpServletRequest request) {

        log.warn("Session not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("SESSION_NOT_FOUND")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    // ── Catch-all ────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.internalServerError().body(ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }
}
