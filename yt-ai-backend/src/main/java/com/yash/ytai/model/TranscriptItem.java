package com.yash.ytai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single transcript item returned by the YouTube transcript scraper.
 *
 * <p>Maps directly to the structure returned by the {@code youtube-transcript} npm library:
 * <pre>
 * { text: "Hello world", offset: 5000, duration: 1200 }
 * </pre>
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code text} — the spoken words in this caption segment</li>
 *   <li>{@code offset} — start time of this caption in <strong>milliseconds</strong></li>
 *   <li>{@code duration} — duration of this caption in <strong>milliseconds</strong></li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptItem {

    private String text;

    /** Start time in milliseconds (matches Node.js {@code item.offset}). */
    private long offset;

    /** Duration of this caption in milliseconds (matches Node.js {@code item.duration}). */
    private long duration;
}
