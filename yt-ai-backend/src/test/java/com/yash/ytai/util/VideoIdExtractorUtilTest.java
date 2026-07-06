package com.yash.ytai.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for VideoIdExtractorUtil.
 */
class VideoIdExtractorUtilTest {

    @Test
    void extract_fullWatchUrl() {
        String result = VideoIdExtractorUtil.extract("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        assertThat(result).isEqualTo("dQw4w9WgXcQ");
    }

    @Test
    void extract_shortUrl() {
        String result = VideoIdExtractorUtil.extract("https://youtu.be/dQw4w9WgXcQ");
        assertThat(result).isEqualTo("dQw4w9WgXcQ");
    }

    @Test
    void extract_bareId() {
        String result = VideoIdExtractorUtil.extract("dQw4w9WgXcQ");
        assertThat(result).isEqualTo("dQw4w9WgXcQ");
    }

    @Test
    void extract_urlWithExtraParams() {
        String result = VideoIdExtractorUtil.extract(
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=30s&list=PL123");
        assertThat(result).isEqualTo("dQw4w9WgXcQ");
    }

    @Test
    void extract_blank_throwsIllegalArgument() {
        assertThatThrownBy(() -> VideoIdExtractorUtil.extract(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void extract_invalidUrl_throwsIllegalArgument() {
        assertThatThrownBy(() -> VideoIdExtractorUtil.extract("https://example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
