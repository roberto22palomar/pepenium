package io.github.roberto22palomar.pepenium.core.runtime;

import io.github.roberto22palomar.pepenium.core.config.PepeniumConfig;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Locale;

final class SessionTimeouts {

    static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    static final Duration DEFAULT_COMMAND_TIMEOUT = Duration.ofMinutes(3);

    private SessionTimeouts() {
    }

    static Duration connectTimeout() {
        return configuredDuration("PEPENIUM_SESSION_CONNECT_TIMEOUT_SECONDS", DEFAULT_CONNECT_TIMEOUT);
    }

    static Duration commandTimeout() {
        return configuredDuration("PEPENIUM_SESSION_COMMAND_TIMEOUT_SECONDS", DEFAULT_COMMAND_TIMEOUT);
    }

    private static Duration configuredDuration(String key, Duration defaultValue) {
        String rawValue = PepeniumConfig.get(key);
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }

        Duration duration = parseDuration(key, rawValue.trim());
        if (duration.isZero() || duration.isNegative()) {
            throw invalid(key, rawValue, null);
        }
        return duration;
    }

    private static Duration parseDuration(String key, String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        try {
            if (normalized.startsWith("p")) {
                return Duration.parse(value.toUpperCase(Locale.ROOT));
            }
            if (normalized.endsWith("ms")) {
                return Duration.ofMillis(Long.parseLong(normalized.substring(0, normalized.length() - 2)));
            }
            if (normalized.endsWith("s")) {
                return Duration.ofSeconds(Long.parseLong(normalized.substring(0, normalized.length() - 1)));
            }
            if (normalized.endsWith("m")) {
                return Duration.ofMinutes(Long.parseLong(normalized.substring(0, normalized.length() - 1)));
            }
            return Duration.ofSeconds(Long.parseLong(normalized));
        } catch (NumberFormatException | DateTimeParseException error) {
            throw invalid(key, value, error);
        }
    }

    private static IllegalStateException invalid(String key, String value, Exception cause) {
        String message = key + " must be a positive duration, but was '" + value
                + "'. Use plain seconds or values like 500ms, 30s, 2m or PT30S.";
        return cause == null ? new IllegalStateException(message) : new IllegalStateException(message, cause);
    }
}
