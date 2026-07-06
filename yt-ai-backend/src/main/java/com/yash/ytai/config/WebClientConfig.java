package com.yash.ytai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient configuration.
 *
 * <p>Provides two pre-configured {@link WebClient} instances:
 * <ul>
 *   <li>{@code geminiWebClient} — base URL + API key query param for Gemini REST API</li>
 *   <li>{@code pineconeWebClient} — base URL + Api-Key header for Pinecone REST API</li>
 * </ul>
 */
@Configuration
public class WebClientConfig {

    @Bean("geminiWebClient")
    public WebClient geminiWebClient(
            @Value("${gemini.api.base-url}") String baseUrl,
            @Value("${gemini.api.key}") String apiKey) {

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-goog-api-key", apiKey)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10 MB — handles large transcript responses
                .build();
    }

    @Bean("pineconeWebClient")
    public WebClient pineconeWebClient(
            @Value("${pinecone.index.host}") String host,
            @Value("${pinecone.api.key}") String apiKey) {

        return WebClient.builder()
                .baseUrl(host)
                .defaultHeader("Api-Key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
