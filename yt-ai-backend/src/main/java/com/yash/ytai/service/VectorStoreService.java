package com.yash.ytai.service;

import com.yash.ytai.model.TranscriptChunk;

import java.util.List;
import java.util.Map;

/**
 * Manages vector storage and retrieval in Pinecone.
 */
public interface VectorStoreService {

    /**
     * Upserts a batch of embedded transcript chunks into the Pinecone index.
     *
     * @param chunks    list of chunks with embeddings already populated
     * @param namespace Pinecone namespace (use sessionId for isolation)
     * @throws com.yash.ytai.exception.VectorStoreException on Pinecone API failure
     */
    void upsertChunks(List<TranscriptChunk> chunks, String namespace);

    /**
     * Queries Pinecone for the top-K most similar vectors.
     * Matches Node.js: {@code pineconeIndex.query({ topK: 5, vector: ..., includeMetadata: true })}
     *
     * @param queryVector 768-dimensional query embedding
     * @param topK        number of results to retrieve
     * @param namespace   Pinecone namespace to search in
     * @return list of matches, each containing metadata (text, startTime, endTime) and score
     */
    List<Map<String, Object>> similaritySearch(List<Float> queryVector, int topK, String namespace);

    /**
     * Deletes all vectors in the given Pinecone namespace.
     * Matches Node.js: {@code index.namespace('').deleteAll()}
     *
     * @param namespace the namespace to clear (use "" for default or sessionId)
     */
    void deleteAllInNamespace(String namespace);
}
