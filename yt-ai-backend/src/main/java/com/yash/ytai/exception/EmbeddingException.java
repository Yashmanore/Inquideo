package com.yash.ytai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when Gemini embedding generation fails.
 * Results in HTTP 502 Bad Gateway (upstream AI service failure).
 */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class EmbeddingException extends RuntimeException {

    public EmbeddingException(String message) {
        super(message);
    }

    public EmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }
}
