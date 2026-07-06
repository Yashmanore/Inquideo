package com.yash.ytai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 * Pinecone configuration.
 *
 * <p>Reads Pinecone connection properties from {@code application.yml}.
 * The {@code host} value is the full HTTPS URL of your Pinecone index,
 * e.g. {@code https://yashmanore-abc123.svc.aped-4627-b74a.pinecone.io}.
 */
@Configuration
@Getter
public class PineconeConfig {

    @Value("${pinecone.api.key}")
    private String apiKey;

    @Value("${pinecone.index.name}")
    private String indexName;

    @Value("${pinecone.index.host}")
    private String indexHost;

    @Value("${pinecone.search.top-k}")
    private int topK;

    @Value("${pinecone.upsert.batch-size}")
    private int upsertBatchSize;
}
