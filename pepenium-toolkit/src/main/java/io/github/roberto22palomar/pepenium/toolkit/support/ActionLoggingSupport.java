package io.github.roberto22palomar.pepenium.toolkit.support;

import io.github.roberto22palomar.pepenium.core.observability.LoggingPreferences;
import io.github.roberto22palomar.pepenium.core.observability.PepeniumTimeline;
import org.slf4j.Logger;

import java.nio.file.Path;

public final class ActionLoggingSupport {

    private ActionLoggingSupport() {
    }

    public static void logTimeout(Logger log, String action, Object target, Throwable throwable) {
        PepeniumTimeline.recordError("Timeout during " + action + ": " + target);
        log.error("Timeout during {}: {}", action, target);
        LoggingPreferences.logDetail(log, action + " stacktrace", throwable);
    }

    public static void logFailure(Logger log, String action, Object target, Throwable throwable) {
        PepeniumTimeline.recordError("Error during " + action + ": " + target + " (" + throwable.getClass().getSimpleName() + ")");
        log.error("Error during {}: {} ({})", action, target, throwable.getClass().getSimpleName());
        LoggingPreferences.logDetail(log, action + " stacktrace", throwable);
    }

    public static void recordAction(String description) {
        if (description == null || description.isBlank()) {
            return;
        }
        PepeniumTimeline.recordAction(description);
    }

    public static void recordWait(String description) {
        if (description == null || description.isBlank()) {
            return;
        }
        PepeniumTimeline.recordWait(description);
    }

    public static Path resolveScreenshotBaseDir() {
        String baseDir = System.getenv("DEVICEFARM_SCREENSHOT_PATH");
        if (baseDir == null || baseDir.isBlank()) {
            baseDir = System.getProperty("java.io.tmpdir");
        }
        return Path.of(baseDir);
    }

    public static void recordSavedScreenshot(String fullPath) {
        if (fullPath == null || fullPath.isBlank()) {
            return;
        }
        PepeniumTimeline.recordScreenshot("Screenshot saved", fullPath);
    }
}
