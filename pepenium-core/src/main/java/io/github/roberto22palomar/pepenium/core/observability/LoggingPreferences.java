package io.github.roberto22palomar.pepenium.core.observability;

import io.github.roberto22palomar.pepenium.core.config.PepeniumConfig;
import org.slf4j.Logger;

public final class LoggingPreferences {

    private static final String ENV_VAR = "PEPENIUM_DETAIL_LOGGING";

    private LoggingPreferences() {
    }

    public static boolean isDetailLoggingEnabled() {
        String value = PepeniumConfig.get(ENV_VAR);
        if (value != null && !value.isBlank()) {
            return Boolean.parseBoolean(value.trim());
        }

        return false;
    }

    public static void logDetail(Logger logger, String message, Throwable throwable) {
        if (isDetailLoggingEnabled()) {
            logger.error("{} [detail logging enabled]", message, throwable);
        }
    }
}
