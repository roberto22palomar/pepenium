package io.github.roberto22palomar.pepenium.core.observability;

import java.nio.file.Path;

public final class ScreenshotPathResolver {

    public static final String SCREENSHOT_PATH_PROPERTY = "pepenium.screenshot.path";
    public static final String SCREENSHOT_PATH_ENV = "PEPENIUM_SCREENSHOT_PATH";
    public static final String LEGACY_SCREENSHOT_PATH_ENV = "DEVICEFARM_SCREENSHOT_PATH";

    private ScreenshotPathResolver() {
    }

    public static Path resolveBaseDir() {
        String baseDir = System.getProperty(SCREENSHOT_PATH_PROPERTY);
        if (isBlank(baseDir)) {
            baseDir = System.getenv(SCREENSHOT_PATH_ENV);
        }
        if (isBlank(baseDir)) {
            baseDir = System.getenv(LEGACY_SCREENSHOT_PATH_ENV);
        }
        if (isBlank(baseDir)) {
            baseDir = System.getProperty("java.io.tmpdir");
        }
        return Path.of(baseDir.trim());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
