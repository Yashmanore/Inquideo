package com.yash.ytai.service;

import com.yash.ytai.model.ChatMessage;

import java.util.List;

/**
 * Manages in-memory multi-turn conversation history per session.
 *
 * <p>Exact port of the Node.js {@code History[]} array from {@code query.js},
 * now stored in a {@link java.util.concurrent.ConcurrentHashMap} keyed by session ID
 * to support concurrent users.
 */
public interface ConversationMemoryService {

    /**
     * Appends a message to the conversation history for the given session.
     *
     * @param sessionId the session identifier
     * @param role      either {@code "user"} or {@code "model"}
     * @param text      the message text
     */
    void addMessage(String sessionId, String role, String text);

    /**
     * Returns the full ordered conversation history for a session.
     *
     * @param sessionId the session identifier
     * @return ordered list of messages (may be empty for new sessions)
     */
    List<ChatMessage> getHistory(String sessionId);

    /**
     * Creates a new session entry.
     *
     * @param sessionId the unique session ID to register
     */
    void initSession(String sessionId);

    /**
     * Checks whether a session exists.
     *
     * @param sessionId the session identifier
     * @return {@code true} if the session is active
     */
    boolean sessionExists(String sessionId);

    /**
     * Clears all messages for the given session and removes it from memory.
     *
     * @param sessionId the session identifier
     */
    void clearSession(String sessionId);
}
