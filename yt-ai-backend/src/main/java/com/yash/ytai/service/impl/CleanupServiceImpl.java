package com.yash.ytai.service.impl;

import com.yash.ytai.service.CleanupService;
import com.yash.ytai.service.ConversationMemoryService;
import com.yash.ytai.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Cleans up all session data — Pinecone vectors + in-memory conversation history.
 *
 * <p>Exact port of Node.js {@code clearDatabase()} from {@code clr.js}:
 * <pre>
 * await index.namespace('').deleteAll();
 * </pre>
 *
 * <p>In the Spring Boot version, we use the {@code sessionId} as the Pinecone namespace
 * (instead of the default {@code ""}) to support concurrent sessions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupServiceImpl implements CleanupService {

    private final VectorStoreService vectorStoreService;
    private final ConversationMemoryService memoryService;

    @Override
    public void cleanupSession(String sessionId) {
        log.info("Cleaning up session: {}", sessionId);

        // 1. Delete all Pinecone vectors in the session namespace
        vectorStoreService.deleteAllInNamespace(sessionId);

        // 2. Clear in-memory conversation history
        memoryService.clearSession(sessionId);

        log.info("✅ Session {} fully cleaned up", sessionId);
    }
}
