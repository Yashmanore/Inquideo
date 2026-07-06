package com.yash.ytai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the YouTube transcript cannot be fetched.
 * Results in HTTP 422 Unprocessable Entity.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class TranscriptFetchException extends RuntimeException {

    public TranscriptFetchException(String message) {
        super(message);
    }

    public TranscriptFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
