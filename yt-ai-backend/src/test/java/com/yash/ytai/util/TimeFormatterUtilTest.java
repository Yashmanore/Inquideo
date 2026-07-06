package com.yash.ytai.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TimeFormatterUtil — verifies exact parity with Node.js formatTime().
 */
class TimeFormatterUtilTest {

    @Test
    void formatTime_exactMatchFromNodeJs_275_28() {
        // From README: 275.28 → "4:35"
        assertThat(TimeFormatterUtil.format(275.28)).isEqualTo("4:35");
    }

    @Test
    void formatTime_exactMatchFromNodeJs_305_28() {
        // From README: 305.28 → "5:05"
        assertThat(TimeFormatterUtil.format(305.28)).isEqualTo("5:05");
    }

    @Test
    void formatTime_zero() {
        assertThat(TimeFormatterUtil.format(0.0)).isEqualTo("0:00");
    }

    @Test
    void formatTime_exactMinute() {
        // 60 seconds → "1:00"
        assertThat(TimeFormatterUtil.format(60.0)).isEqualTo("1:00");
    }

    @Test
    void formatTime_singleDigitSeconds() {
        // 65 seconds → "1:05" (padded)
        assertThat(TimeFormatterUtil.format(65.0)).isEqualTo("1:05");
    }

    @Test
    void formatTime_largeValue() {
        // 3600 seconds (1 hour) → "60:00"
        assertThat(TimeFormatterUtil.format(3600.0)).isEqualTo("60:00");
    }
}
