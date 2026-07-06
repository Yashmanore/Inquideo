package com.yash.ytai.service.impl;

import com.yash.ytai.model.ChatMessage;
import com.yash.ytai.service.ConversationMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory conversation history store — exact port of the Node.js {@code History[]} array.
 *
 * <p>Uses a {@link ConcurrentHashMap} keyed by {@code sessionId} to support concurrent users,
 * whereas the original Node.js version had a single global {@code History} array per process.
 *
 * <p>Sessions are lost on server restart (same behavior as the Node.js CLI).
 * For persistence across restarts, integrate Redis in a future iteration.
 */
@Service
@Slf4j
public class ConversationMemoryServiceImpl implements ConversationMemoryService {

    private final ConcurrentHashMap<String, List<ChatMessage>> sessions = new ConcurrentHashMap<>();

    @Override
    public void initSession(String sessionId) {
        sessions.put(sessionId, Collections.synchronizedList(new ArrayList<>()));
        log.debug("Initialized new session: {}", sessionId);
    }

    @Override
    public void addMessage(String sessionId, String role, String text) {
        List<ChatMessage> history = sessions.get(sessionId);
        if (history == null) {
            log.warn("addMessage called for non-existent session: {}", sessionId);
            initSession(sessionId);
            history = sessions.get(sessionId);
        }
        history.add(ChatMessage.builder().role(role).text(text).build());
        log.debug("Added {} message to session {}; history size={}", role, sessionId, history.size());
    }

    @Override
    public List<ChatMessage> getHistory(String sessionId) {
        List<ChatMessage> history = sessions.get(sessionId);
        return history != null ? Collections.unmodifiableList(history) : List.of();
    }

    @Override
    public boolean sessionExists(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    @Override
    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
        log.debug("Cleared session: {}", sessionId);
    }
}
