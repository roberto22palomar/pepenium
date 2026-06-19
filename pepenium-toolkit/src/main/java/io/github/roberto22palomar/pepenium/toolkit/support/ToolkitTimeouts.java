package io.github.roberto22palomar.pepenium.toolkit.support;

import java.time.Duration;

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
            rawValue = System.getenv(envKey);
            source = envKey;
        }
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        String value = rawValue.trim();
        try {
            long seconds = Long.parseLong(value);
            if (seconds < 1L) {
                throw new IllegalStateException(source + " must be at least 1 second.");
            }
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(source + " must be a positive integer number of seconds.", e);
        }
    }
}
