package com.yash.ytai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * YT-AI — YouTube RAG Chatbot
 *
 * <p>Spring Boot entry point. Bootstraps the full RAG pipeline:
 * Transcript → Chunking → Embedding → Pinecone → Gemini Q&A
 */
@SpringBootApplication
public class YtAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(YtAiApplication.class, args);
    }
}
