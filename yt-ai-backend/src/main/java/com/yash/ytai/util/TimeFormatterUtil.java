package com.yash.ytai.util;

/**
 * Utility class for formatting timestamps.
 *
 * <p>Exact port of the Node.js {@code formatTime(seconds)} function from {@code query.js}:
 * <pre>
 * function formatTime(seconds) {
 *     const m = Math.floor(seconds / 60);
 *     const s = Math.floor(seconds % 60).toString().padStart(2, '0');
 *     return `${m}:${s}`;
 * }
 * </pre>
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code 275.28} → {@code "4:35"}</li>
 *   <li>{@code 305.28} → {@code "5:05"}</li>
 *   <li>{@code 0.0}    → {@code "0:00"}</li>
 * </ul>
 */
public final class TimeFormatterUtil {

    private TimeFormatterUtil() {
        // Utility class — no instantiation
    }

    /**
     * Formats a time value in seconds to M:SS string format.
     *
     * @param seconds the time in seconds (may be fractional)
     * @return formatted string, e.g. {@code "4:35"}
     */
    public static String format(double seconds) {
        int totalSeconds = (int) Math.floor(seconds);
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", secs);
    }
}
