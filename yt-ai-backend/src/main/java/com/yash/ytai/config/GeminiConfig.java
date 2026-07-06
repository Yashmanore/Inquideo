package com.yash.ytai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 * Gemini API configuration.
 *
 * <p>Reads Gemini-related properties from {@code application.yml} and makes them
 * injectable throughout the application via {@link org.springframework.beans.factory.annotation.Autowired}.
 */
@Configuration
@Getter
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base-url}")
    private String baseUrl;

    @Value("${gemini.model.embedding}")
    private String embeddingModel;

    @Value("${gemini.model.generation}")
    private String generationModel;

    @Value("${gemini.embedding.output-dimensionality}")
    private int outputDimensionality;
}
