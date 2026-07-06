package com.yash.ytai.service;

import java.util.List;

/**
 * Generates 768-dimensional text embeddings using Gemini {@code gemini-embedding-001}.
 */
public interface EmbeddingService {

    /**
     * Generates a document embedding optimized for retrieval storage.
     * Matches Node.js: {@code taskType: "RETRIEVAL_DOCUMENT", outputDimensionality: 768}
     *
     * @param text the text to embed
     * @return 768-dimensional float vector
     * @throws com.yash.ytai.exception.EmbeddingException on API failure
     */
    List<Float> embedDocument(String text);

    /**
     * Generates a query embedding optimized for similarity search.
     * Matches Node.js: {@code taskType: "RETRIEVAL_QUERY", outputDimensionality: 768}
     *
     * @param text the query text to embed
     * @return 768-dimensional float vector
     * @throws com.yash.ytai.exception.EmbeddingException on API failure
     */
    List<Float> embedQuery(String text);
}
