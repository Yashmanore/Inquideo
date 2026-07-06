package com.yash.ytai.service;

import com.yash.ytai.dto.response.ChatResponse;

/**
 * Orchestrates a full RAG chat turn.
 */
public interface ChatService {

    /**
     * Processes a user question using the RAG pipeline.
     *
     * <p>Exact port of Node.js {@code chatting(question)}:
     * <ol>
     *   <li>Embed the question (RETRIEVAL_QUERY, 768-dim)</li>
     *   <li>Query Pinecone topK=5 in the session namespace</li>
     *   <li>Build context string with formatted timestamps</li>
     *   <li>Push user turn to conversation history</li>
     *   <li>Call Gemini 2.5 Flash with history + system prompt</li>
     *   <li>Push model turn to history</li>
     *   <li>Return answer + source citations</li>
     * </ol>
     *
     * @param sessionId the active session identifier
     * @param question  the user's question
     * @return {@link ChatResponse} containing the answer and source citations
     */
    ChatResponse chat(String sessionId, String question);
}
