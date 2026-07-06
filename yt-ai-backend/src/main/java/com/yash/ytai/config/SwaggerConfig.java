package com.yash.ytai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc / Swagger configuration.
 *
 * <p>Swagger UI available at: {@code http://localhost:8080/swagger-ui.html}
 * <p>OpenAPI JSON available at: {@code http://localhost:8080/api-docs}
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI ytAiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("YT-AI — YouTube RAG Chatbot API")
                        .description("""
                                Production-grade RAG API that allows users to ask questions about YouTube videos.
                                
                                **Pipeline:**
                                1. `POST /api/v1/video/process` — Fetch transcript → Chunk → Embed → Upsert to Pinecone
                                2. `POST /api/v1/chat` — Embed query → Retrieve → Generate Gemini response with timestamp citations
                                3. `GET /api/v1/chat/history/{sessionId}` — Retrieve conversation history
                                4. `DELETE /api/v1/cleanup/{sessionId}` — Delete Pinecone vectors + clear session
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Yash Manore")
                                .email("yash@ytai.dev"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")));
    }
}
