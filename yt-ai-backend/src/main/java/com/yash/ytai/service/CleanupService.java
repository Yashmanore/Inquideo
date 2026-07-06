package com.yash.ytai.service;

/**
 * Cleans up Pinecone vector storage and session data.
 */
public interface CleanupService {

    /**
     * Deletes all vectors in the given Pinecone namespace and clears session history.
     * Matches Node.js: {@code index.namespace('').deleteAll()}
     *
     * @param sessionId the session to clean up (used as Pinecone namespace)
     */
    void cleanupSession(String sessionId);
}
