package io.github.roberto22palomar.pepenium.core.runtime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionTimeoutsTest {

    @AfterEach
    void clearProperties() {
        System.clearProperty("pepenium.session.connect.timeout.seconds");
        System.clearProperty("pepenium.session.command.timeout.seconds");
    }

    @Test
    void usesCloudSafeDefaults() {
        assertEquals(Duration.ofSeconds(10), SessionTimeouts.connectTimeout());
        assertEquals(Duration.ofMinutes(3), SessionTimeouts.commandTimeout());
    }

    @Test
    void acceptsSecondsUnitsAndIsoDurations() {
        System.setProperty("pepenium.session.connect.timeout.seconds", "750ms");
        System.setProperty("pepenium.session.command.timeout.seconds", "PT2M");

        assertEquals(Duration.ofMillis(750), SessionTimeouts.connectTimeout());
        assertEquals(Duration.ofMinutes(2), SessionTimeouts.commandTimeout());
    }

    @Test
    void rejectsInvalidOrNonPositiveDurationsBeforeCreatingADriver() {
        System.setProperty("pepenium.session.connect.timeout.seconds", "0s");
        IllegalStateException zero = assertThrows(IllegalStateException.class, SessionTimeouts::connectTimeout);
        assertTrue(zero.getMessage().contains("must be a positive duration"));

        System.setProperty("pepenium.session.connect.timeout.seconds", "eventually");
        IllegalStateException invalid = assertThrows(IllegalStateException.class, SessionTimeouts::connectTimeout);
        assertTrue(invalid.getMessage().contains("500ms, 30s, 2m or PT30S"));
    }
}
