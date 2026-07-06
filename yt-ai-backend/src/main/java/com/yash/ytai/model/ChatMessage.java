package com.yash.ytai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single turn in the Gemini multi-turn conversation history.
 *
 * <p>Maps directly to the Gemini API Content object used by the Node.js {@code History} array:
 * <pre>
 * { role: 'user' | 'model', parts: [{ text: "..." }] }
 * </pre>
 *
 * <p>Valid roles: {@code "user"} and {@code "model"} (Gemini convention).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /** Either {@code "user"} or {@code "model"} — matches Gemini API role naming. */
    private String role;

    /** The message text content. */
    private String text;
}
