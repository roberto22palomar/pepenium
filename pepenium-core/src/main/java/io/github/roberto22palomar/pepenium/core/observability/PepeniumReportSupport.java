package io.github.roberto22palomar.pepenium.core.observability;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class PepeniumReportSupport {

    static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static final Yaml YAML = new Yaml();

    private PepeniumReportSupport() {
    }

    static String buildBaseFileName(PepeniumHtmlReportWriter.ReportContext report) {
        String timestamp = FILE_TIME_FORMAT.format(report.finishedAt.atZone(ZoneId.systemDefault()));
        return String.format(
                Locale.ROOT,
                "report-%s-%s-%s",
                timestamp,
                report.outcome.toLowerCase(Locale.ROOT),
                sanitizeFileName(report.testName)
        );
    }

    static void appendJsonField(StringBuilder json, String key, Object value, boolean withComma) {
        appendJsonField(json, key, value, withComma, 2);
    }

    static void appendJsonField(StringBuilder json, String key, Object value, boolean withComma, int indent) {
        json.append(" ".repeat(indent)).append(quoteJson(key)).append(": ");
        if (value == null) {
            json.append("null");
        } else if (value instanceof Number || value instanceof Boolean) {
            json.append(value);
        } else {
            json.append(quoteJson(String.valueOf(value)));
        }
        if (withComma) {
            json.append(",");
        }
        json.append("\n");
    }

    static String quoteJson(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t") + "\"";
    }

    static Map<String, Object> mapValue(Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cast = (Map<String, Object>) value;
            return cast;
        }
        return new LinkedHashMap<>();
    }

    static long numberValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    static String formatDuration(Instant startedAt, Instant finishedAt) {
        return formatDurationMillis(Duration.between(startedAt, finishedAt).toMillis());
    }

    static String formatDurationMillis(long millis) {
        Duration duration = Duration.ofMillis(Math.max(millis, 0L));
        long totalSeconds = duration.getSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long remainingMillis = duration.toMillisPart();
        if (minutes > 0) {
            return String.format(Locale.ROOT, "%dm %02ds %03dms", minutes, seconds, remainingMillis);
        }
        return String.format(Locale.ROOT, "%ds %03dms", seconds, remainingMillis);
    }

    static Instant lastModifiedSafely(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException e) {
            return Instant.EPOCH;
        }
    }

    static String screenshotUri(WebDriver driver, Path reportDir) {
        if (!(driver instanceof TakesScreenshot)) {
            return null;
        }
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path screenshotDir = reportDir.resolve("screenshots");
            Files.createDirectories(screenshotDir);
            Path screenshotPath = screenshotDir.resolve("report_" + Instant.now().toEpochMilli() + ".png");
            Files.write(screenshotPath, screenshot);
            return screenshotPath.toUri().toString();
        } catch (Exception e) {
            return null;
        }
    }

    static String pathToUri(String path) {
        return path == null ? null : Path.of(path).toUri().toString();
    }

    static Path resolveReportDir() {
        String override = System.getProperty("pepenium.report.dir");
        if (override == null || override.isBlank()) {
            override = System.getenv("PEPENIUM_REPORT_DIR");
        }
        return override == null || override.isBlank()
                ? reportDirHint()
                : Paths.get(override);
    }

    private static Path reportDirHint() {
        return Paths.get("target", "pepenium-reports");
    }

    static String sessionId(WebDriver driver) {
        if (driver instanceof RemoteWebDriver) {
            try {
                return String.valueOf(((RemoteWebDriver) driver).getSessionId());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    static String currentUrl(WebDriver driver) {
        try {
            return driver == null ? null : driver.getCurrentUrl();
        } catch (Exception ignored) {
            return null;
        }
    }

    static String pageTitle(WebDriver driver) {
        try {
            return driver == null ? null : driver.getTitle();
        } catch (Exception ignored) {
            return null;
        }
    }

    static String mobileContext(WebDriver driver) {
        if (!(driver instanceof AppiumDriver)) {
            return null;
        }
        try {
            Object context = driver.getClass().getMethod("getContext").invoke(driver);
            return context == null ? null : String.valueOf(context);
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return null;
        }
    }

    static String mobilePackage(WebDriver driver) {
        if (!(driver instanceof AndroidDriver)) {
            return null;
        }
        try {
            return ((AndroidDriver) driver).getCurrentPackage();
        } catch (Exception ignored) {
            return null;
        }
    }

    static String mobileActivity(WebDriver driver) {
        if (driver instanceof AndroidDriver) {
            try {
                return ((AndroidDriver) driver).currentActivity();
            } catch (Exception ignored) {
                return null;
            }
        }
        if (driver instanceof IOSDriver) {
            return "n/a";
        }
        return null;
    }

    static String rootType(Throwable cause) {
        Throwable root = rootCause(cause);
        return root == null ? null : root.getClass().getSimpleName();
    }

    static String rootMessage(Throwable cause) {
        Throwable root = rootCause(cause);
        if (root == null || root.getMessage() == null || root.getMessage().isBlank()) {
            return null;
        }
        return root.getMessage().replaceAll("\\s+", " ").trim();
    }

    static String stackTrace(Throwable cause) {
        if (cause == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    static String safe(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    static String defaultValue(String value) {
        return value == null || value.isBlank() ? "n/a" : value;
    }

    static String sanitizeFileName(String input) {
        String sanitized = input == null ? "unnamed-test" : input.replaceAll("[^a-zA-Z0-9._-]+", "_");
        return sanitized.isBlank() ? "unnamed-test" : sanitized;
    }

    static String escapeHtml(String raw) {
        if (raw == null) {
            return "n/a";
        }
        return raw
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    static String capabilityString(Capabilities capabilities, String capabilityKey, String nestedKey) {
        if (capabilities == null) {
            return null;
        }
        Object value = capabilities.getCapability(capabilityKey);
        if (nestedKey == null) {
            return value == null ? null : String.valueOf(value);
        }
        if (value instanceof Map) {
            Object nested = ((Map<?, ?>) value).get(nestedKey);
            return nested == null ? null : String.valueOf(nested);
        }
        return null;
    }

    static String firstCapability(Capabilities capabilities, String... keys) {
        if (capabilities == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = capabilities.getCapability(key);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    static String sanitizeServerUrl(URL url) {
        if (url == null) {
            return null;
        }
        String protocol = url.getProtocol() == null ? "http" : url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();
        String path = url.getPath() == null ? "" : url.getPath();
        StringBuilder value = new StringBuilder(protocol).append("://").append(host);
        if (port > 0) {
            value.append(":").append(port);
        }
        value.append(path);
        return value.toString();
    }
}
