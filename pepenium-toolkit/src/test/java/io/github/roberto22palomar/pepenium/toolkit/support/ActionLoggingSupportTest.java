package io.github.roberto22palomar.pepenium.toolkit.support;

import io.github.roberto22palomar.pepenium.core.observability.ScreenshotPathResolver;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ActionLoggingSupportTest {

    @Test
    void resolveScreenshotBaseDirUsesDedicatedPepeniumPropertyWhenPresent() {
        String previous = System.getProperty(ScreenshotPathResolver.SCREENSHOT_PATH_PROPERTY);
        System.setProperty(ScreenshotPathResolver.SCREENSHOT_PATH_PROPERTY, "C:\\temp\\pepenium-shots");

        try {
            Path resolved = ActionLoggingSupport.resolveScreenshotBaseDir();
            assertEquals(Path.of("C:\\temp\\pepenium-shots"), resolved);
        } finally {
            if (previous == null) {
                System.clearProperty(ScreenshotPathResolver.SCREENSHOT_PATH_PROPERTY);
            } else {
                System.setProperty(ScreenshotPathResolver.SCREENSHOT_PATH_PROPERTY, previous);
            }
        }
    }

    @Test
    void resolveScreenshotBaseDirFallsBackToJavaTmpDir() {
        Path resolved = ActionLoggingSupport.resolveScreenshotBaseDir();

        assertEquals(Path.of(System.getProperty("java.io.tmpdir")), resolved);
    }

    @Test
    void logTimeoutUsesLoggerError() {
        Logger logger = mock(Logger.class);

        ActionLoggingSupport.logTimeout(logger, "click", "target", new RuntimeException("boom"));

        verify(logger).error("Timeout during {}: {}", "click", "target");
    }

    @Test
    void logFailureUsesLoggerError() {
        Logger logger = mock(Logger.class);

        ActionLoggingSupport.logFailure(logger, "type", "field", new IllegalStateException("bad"));

        verify(logger).error("Error during {}: {} ({})", "type", "field", "IllegalStateException");
    }
}
