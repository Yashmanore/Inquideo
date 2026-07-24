package com.yash.ytai.controller;

import com.yash.ytai.dto.request.ProcessVideoRequest;
import com.yash.ytai.dto.response.ProcessVideoResponse;
import com.yash.ytai.service.VideoProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for YouTube video processing.
 *
 * <p>Exposes the entry point of the RAG pipeline — submit a YouTube URL to trigger
 * transcript fetching, chunking, embedding, and Pinecone upsert.
 */
@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Video Processing", description = "Process YouTube videos for RAG chat")
public class VideoProcessingController {

    private final VideoProcessingService videoProcessingService;

    /**
     * POST /api/v1/video/process
     *
     * <p>Triggers the full ingestion pipeline for a YouTube video.
     * Returns a {@code sessionId} to use for subsequent chat requests.
     */
    @PostMapping("/process")
    @Operation(
            summary = "Process a YouTube video",
            description = """
                    Fetches the YouTube transcript, chunks it with a 30s sliding window (5s overlap),
                    generates 768-dim Gemini embeddings, and stores them in Pinecone.
                    Returns a sessionId for chatting.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Video processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid YouTube URL"),
            @ApiResponse(responseCode = "422", description = "Transcript unavailable for this video"),
            @ApiResponse(responseCode = "502", description = "Gemini or Pinecone API error")
    })
    public ResponseEntity<ProcessVideoResponse> processVideo(
            @Valid @RequestBody ProcessVideoRequest request) {

        log.info("Received video processing request: {}", request.getVideoUrl());
        ProcessVideoResponse response = videoProcessingService.processVideo(request.getVideoUrl());
        return ResponseEntity.ok(response);
    }

    /**
     * Diagnostic endpoint to test direct connectivity from the deployed server to the public transcript API.
     */
    @GetMapping("/test-fetch/{videoId}")
    public ResponseEntity<String> testFetch(@PathVariable String videoId) {
        try {
            org.springframework.web.reactive.function.client.WebClient client = 
                org.springframework.web.reactive.function.client.WebClient.builder().build();
            String url = "https://youtube-transcript.ai/transcript/" + videoId + ".txt?lang=en";
            String response = client.get()
                    .uri(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok("SUCCESS:\n" + response);
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            return ResponseEntity.status(500).body("ERROR: " + e.getMessage() + "\nSTACKTRACE:\n" + sw.toString());
        }
    }
}
