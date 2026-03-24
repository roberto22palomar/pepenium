package io.github.roberto22palomar.pepenium.core;

import org.slf4j.Logger;

public final class LoggingPreferences {

    private static final String SYS_PROP = "pepenium.detail.logging";
    private static final String ENV_VAR = "PEPENIUM_DETAIL_LOGGING";

    private LoggingPreferences() {
    }

    public static boolean isDetailLoggingEnabled() {
        String systemValue = System.getProperty(SYS_PROP);
        if (systemValue != null && !systemValue.isBlank()) {
            return Boolean.parseBoolean(systemValue.trim());
        }

        String envValue = System.getenv(ENV_VAR);
        if (envValue != null && !envValue.isBlank()) {
            return Boolean.parseBoolean(envValue.trim());
        }

        return false;
    }

    public static void logDetail(Logger logger, String message, Throwable throwable) {
        if (isDetailLoggingEnabled()) {
            logger.error("{} [detail logging enabled]", message, throwable);
        }
    }
}
