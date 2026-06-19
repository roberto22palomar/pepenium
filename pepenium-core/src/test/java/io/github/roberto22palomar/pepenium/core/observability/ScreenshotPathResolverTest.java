package io.github.roberto22palomar.pepenium.core.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScreenshotPathResolverTest {

    @TempDir
    private Path tempDir;

    private String previousTmpDir;

    @BeforeEach
    void setUp() {
        previousTmpDir = System.getProperty("java.io.tmpdir");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(ScreenshotPathResolver.SCREENSHOT_PATH_PROPERTY);
        if (previousTmpDir == null) {
            System.clearProperty("java.io.tmpdir");
        } else {
            System.setProperty("java.io.tmpdir", previousTmpDir);
        }
    }

    @Test
    void resolveBaseDirTrimsSystemPropertyOverride() {
        Path screenshotDir = tempDir.resolve("screenshots");
        System.setProperty(ScreenshotPathResolver.SCREENSHOT_PATH_PROPERTY, "  " + screenshotDir + "  ");

        assertEquals(screenshotDir, ScreenshotPathResolver.resolveBaseDir());
    }

    @Test
    void resolveBaseDirUsesJavaTempDirFallback() {
        System.setProperty("java.io.tmpdir", tempDir.toString());

        assertEquals(tempDir, ScreenshotPathResolver.resolveBaseDir());
    }
}
