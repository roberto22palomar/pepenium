package io.github.roberto22palomar.pepenium.core.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PepeniumReportSupportTest {

    @TempDir
    private Path tempDir;

    @AfterEach
    void tearDown() {
        System.clearProperty("pepenium.report.dir");
    }

    @Test
    void resolveReportDirTrimsSystemPropertyOverride() {
        Path reportDir = tempDir.resolve("reports");
        System.setProperty("pepenium.report.dir", "  " + reportDir + "  ");

        assertEquals(reportDir, PepeniumReportSupport.resolveReportDir());
    }

    @Test
    void pathToHrefRelativizesFilesInsideReportDirectory() {
        Path reportDir = tempDir.resolve("reports");
        Path screenshot = reportDir.resolve("screenshots").resolve("failure.png");

        assertEquals("screenshots/failure.png", PepeniumReportSupport.pathToHref(screenshot.toString(), reportDir));
    }

    @Test
    void pathToHrefKeepsExternalAbsoluteFilesAsUri() {
        Path reportDir = tempDir.resolve("reports");
        Path external = tempDir.resolve("external").resolve("manual.png");

        assertEquals(external.toUri().toString(), PepeniumReportSupport.pathToHref(external.toString(), reportDir));
    }

    @Test
    void failureDetailsAreSanitizedBeforeReportPersistence() {
        RuntimeException failure = new RuntimeException(
                "Grid https://user:secret@hub.example.test failed with accessKey=abc123"
        );

        String message = PepeniumReportSupport.rootMessage(failure);
        String stackTrace = PepeniumReportSupport.stackTrace(failure);

        assertEquals("Grid https://***@hub.example.test failed with accessKey=***", message);
        assertFalse(stackTrace.contains("user:secret"));
        assertFalse(stackTrace.contains("abc123"));
    }
}
