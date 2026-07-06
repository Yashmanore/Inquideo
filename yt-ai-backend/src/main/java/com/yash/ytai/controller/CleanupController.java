package com.yash.ytai.controller;

import com.yash.ytai.dto.response.CleanupResponse;
import com.yash.ytai.service.CleanupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for session cleanup.
 *
 * <p>Deletes all Pinecone vectors for the session namespace and clears
 * the in-memory conversation history. Mirrors the Node.js end-of-session cleanup.
 */
@RestController
@RequestMapping("/api/v1/cleanup")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cleanup", description = "Clean up Pinecone vectors and session data")
public class CleanupController {

    private final CleanupService cleanupService;

    /**
     * DELETE /api/v1/cleanup/{sessionId}
     *
     * <p>Exact port of Node.js:
     * <pre>
     * await clearDatabase();  // index.namespace('').deleteAll()
     * </pre>
     */
    @DeleteMapping("/{sessionId}")
    @Operation(
            summary = "Clean up a session",
            description = """
                    Deletes all vector embeddings from the Pinecone namespace associated with
                    this session, and clears the in-memory conversation history.
                    Equivalent to the Node.js 'exit' + `clearDatabase()` flow.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session cleaned up successfully"),
            @ApiResponse(responseCode = "502", description = "Pinecone API error during deletion")
    })
    public ResponseEntity<CleanupResponse> cleanup(
            @Parameter(description = "Session ID to clean up",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionId) {

        log.info("Cleanup request for sessionId: {}", sessionId);
        cleanupService.cleanupSession(sessionId);

        return ResponseEntity.ok(CleanupResponse.builder()
                .sessionId(sessionId)
                .message("Session cleaned up successfully. Pinecone namespace cleared.")
                .build());
    }
}
