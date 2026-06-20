package io.github.roberto22palomar.pepenium.toolkit.support;

import io.github.roberto22palomar.pepenium.core.config.PepeniumConfig;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class ToolkitTimeouts {

    public static final String ACTION_TIMEOUT_PROPERTY = "pepenium.action.timeout.seconds";
    public static final String ACTION_TIMEOUT_ENV = "PEPENIUM_ACTION_TIMEOUT_SECONDS";
    public static final String LONG_ACTION_TIMEOUT_PROPERTY = "pepenium.action.long-timeout.seconds";
    public static final String LONG_ACTION_TIMEOUT_ENV = "PEPENIUM_ACTION_LONG_TIMEOUT_SECONDS";
    public static final String ASSERTION_TIMEOUT_PROPERTY = "pepenium.assertion.timeout.seconds";
    public static final String ASSERTION_TIMEOUT_ENV = "PEPENIUM_ASSERTION_TIMEOUT_SECONDS";

    private ToolkitTimeouts() {
    }

    public static Duration actionTimeout(Duration defaultValue) {
        return configuredDuration(ACTION_TIMEOUT_PROPERTY, ACTION_TIMEOUT_ENV, defaultValue);
    }

    public static Duration longActionTimeout(Duration defaultValue) {
        return configuredDuration(LONG_ACTION_TIMEOUT_PROPERTY, LONG_ACTION_TIMEOUT_ENV, defaultValue);
    }

    public static Duration assertionTimeout(Duration defaultValue) {
        return configuredDuration(ASSERTION_TIMEOUT_PROPERTY, ASSERTION_TIMEOUT_ENV, defaultValue);
    }

    private static Duration configuredDuration(String systemProperty, String envKey, Duration defaultValue) {
        String rawValue = System.getProperty(systemProperty);
        String source = systemProperty;
        if (rawValue == null || rawValue.isBlank()) {
            rawValue = PepeniumConfig.get(envKey);
            source = envKey;
        }
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        String value = rawValue.trim();
        Duration parsed = parseDuration(source, value);
        if (parsed.isZero() || parsed.isNegative()) {
            throw new IllegalStateException(source + " must be greater than 0.");
        }
        return parsed;
    }

    private static Duration parseDuration(String source, String value) {
        try {
            return Duration.ofSeconds(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return parseDurationWithUnit(source, value, e);
        }
    }

    private static Duration parseDurationWithUnit(String source, String value, NumberFormatException cause) {
        String normalized = value.toLowerCase(Locale.ROOT);
        try {
            if (normalized.startsWith("p")) {
                return Duration.parse(value);
            }
            if (normalized.endsWith("ms")) {
                return Duration.ofMillis(Long.parseLong(normalized.substring(0, normalized.length() - 2).trim()));
            }
            if (normalized.endsWith("s")) {
                return Duration.ofSeconds(Long.parseLong(normalized.substring(0, normalized.length() - 1).trim()));
            }
            if (normalized.endsWith("m")) {
                return Duration.ofMinutes(Long.parseLong(normalized.substring(0, normalized.length() - 1).trim()));
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            cause.addSuppressed(e);
        }
        throw new IllegalStateException(source
                + " must be a positive duration. Use plain seconds or values like 500ms, 2s, 1m or PT2S.",
                cause);
    }
}
