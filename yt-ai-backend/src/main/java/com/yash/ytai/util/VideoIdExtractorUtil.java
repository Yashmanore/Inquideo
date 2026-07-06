package com.yash.ytai.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting YouTube video IDs.
 *
 * <p>Handles all common YouTube URL formats and bare video IDs:
 * <ul>
 *   <li>{@code https://www.youtube.com/watch?v=dQw4w9WgXcQ}</li>
 *   <li>{@code https://youtu.be/dQw4w9WgXcQ}</li>
 *   <li>{@code https://www.youtube.com/embed/dQw4w9WgXcQ}</li>
 *   <li>{@code dQw4w9WgXcQ} (bare 11-char ID)</li>
 * </ul>
 */
public final class VideoIdExtractorUtil {

    /**
     * Regex that matches the 11-character video ID in all known YouTube URL formats.
     * Also matches a standalone 11-character ID (bare input).
     */
    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?v=|embed/|v/)|youtu\\.be/)([a-zA-Z0-9_-]{11})" +
            "|^([a-zA-Z0-9_-]{11})$"
    );

    private VideoIdExtractorUtil() {
        // Utility class
    }

    /**
     * Extracts the 11-character YouTube video ID from a URL or bare ID.
     *
     * @param urlOrId the YouTube URL or bare video ID entered by the user
     * @return the 11-character video ID
     * @throws IllegalArgumentException if the input does not contain a valid video ID
     */
    public static String extract(String urlOrId) {
        if (urlOrId == null || urlOrId.isBlank()) {
            throw new IllegalArgumentException("Video URL or ID must not be blank");
        }

        String trimmed = urlOrId.trim();
        Matcher matcher = VIDEO_ID_PATTERN.matcher(trimmed);

        if (matcher.find()) {
            // Group 1: from full URL; Group 2: bare 11-char ID
            String id = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            return id;
        }

        throw new IllegalArgumentException(
                "Could not extract a valid YouTube video ID from: " + urlOrId);
    }
}
