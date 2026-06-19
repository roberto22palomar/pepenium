package io.github.roberto22palomar.pepenium.toolkit.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToolkitTimeoutsTest {

    @AfterEach
    void tearDown() {
        System.clearProperty(ToolkitTimeouts.ACTION_TIMEOUT_PROPERTY);
        System.clearProperty(ToolkitTimeouts.LONG_ACTION_TIMEOUT_PROPERTY);
        System.clearProperty(ToolkitTimeouts.ASSERTION_TIMEOUT_PROPERTY);
    }

    @Test
    void actionTimeoutUsesDefaultWhenNoOverrideExists() {
        assertEquals(Duration.ofSeconds(6), ToolkitTimeouts.actionTimeout(Duration.ofSeconds(6)));
    }

    @Test
    void actionTimeoutUsesSystemPropertyOverride() {
        System.setProperty(ToolkitTimeouts.ACTION_TIMEOUT_PROPERTY, "12");

        assertEquals(Duration.ofSeconds(12), ToolkitTimeouts.actionTimeout(Duration.ofSeconds(6)));
    }

    @Test
    void actionTimeoutAcceptsExplicitMilliseconds() {
        System.setProperty(ToolkitTimeouts.ACTION_TIMEOUT_PROPERTY, "750ms");

        assertEquals(Duration.ofMillis(750), ToolkitTimeouts.actionTimeout(Duration.ofSeconds(6)));
    }

    @Test
    void longActionTimeoutAcceptsExplicitSecondsAndMinutes() {
        System.setProperty(ToolkitTimeouts.LONG_ACTION_TIMEOUT_PROPERTY, "2m");

        assertEquals(Duration.ofMinutes(2), ToolkitTimeouts.longActionTimeout(Duration.ofSeconds(120)));

        System.setProperty(ToolkitTimeouts.LONG_ACTION_TIMEOUT_PROPERTY, "90s");

        assertEquals(Duration.ofSeconds(90), ToolkitTimeouts.longActionTimeout(Duration.ofSeconds(120)));
    }

    @Test
    void assertionTimeoutAcceptsIsoDuration() {
        System.setProperty(ToolkitTimeouts.ASSERTION_TIMEOUT_PROPERTY, "PT1.5S");

        assertEquals(Duration.ofMillis(1500), ToolkitTimeouts.assertionTimeout(Duration.ofSeconds(6)));
    }

    @Test
    void longActionTimeoutRejectsNonPositiveValues() {
        System.setProperty(ToolkitTimeouts.LONG_ACTION_TIMEOUT_PROPERTY, "0");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> ToolkitTimeouts.longActionTimeout(Duration.ofSeconds(120))
        );

        assertEquals(
                ToolkitTimeouts.LONG_ACTION_TIMEOUT_PROPERTY + " must be greater than 0.",
                error.getMessage()
        );
    }

    @Test
    void assertionTimeoutRejectsMalformedValues() {
        System.setProperty(ToolkitTimeouts.ASSERTION_TIMEOUT_PROPERTY, "slow");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> ToolkitTimeouts.assertionTimeout(Duration.ofSeconds(6))
        );

        assertEquals(
                ToolkitTimeouts.ASSERTION_TIMEOUT_PROPERTY
                        + " must be a positive duration. Use plain seconds or values like 500ms, 2s, 1m or PT2S.",
                error.getMessage()
        );
    }
}
