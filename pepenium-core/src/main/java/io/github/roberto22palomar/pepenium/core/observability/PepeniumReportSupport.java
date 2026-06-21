package io.github.roberto22palomar.pepenium.core.observability;

import io.github.roberto22palomar.pepenium.core.config.PepeniumConfig;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
    static final Yaml YAML = createSafeYamlParser();

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
            Path screenshotPath = screenshotDir.resolve(uniqueArtifactStem("report") + ".png");
            Files.write(screenshotPath, screenshot);
            return pathToHref(screenshotPath.toString(), reportDir);
        } catch (Exception e) {
            return null;
        }
    }

    static String bundleScreenshotArtifact(String originalPath, Path reportDir) {
        if (originalPath == null || originalPath.isBlank()) {
            return null;
        }
        if (reportDir == null) {
            return originalPath;
        }
        try {
            Path source = Path.of(originalPath).normalize();
            if (!Files.exists(source) || Files.isDirectory(source)) {
                return originalPath;
            }
            Path screenshotDir = reportDir.resolve("screenshots");
            Files.createDirectories(screenshotDir);
            Path fileNamePath = source.getFileName();
            String fileName = "manual.png";
            if (fileNamePath != null) {
                fileName = fileNamePath.toString();
            }
            Path target = screenshotDir.resolve(uniqueArtifactStem("manual") + "_" + fileName).normalize();
            if (!source.equals(target)) {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target.toString();
        } catch (Exception ignored) {
            return originalPath;
        }
    }

    static String pathToHref(String path, Path reportDir) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            Path resolved = Path.of(path).normalize();
            if (reportDir != null && resolved.isAbsolute()) {
                Path normalizedReportDir = reportDir.toAbsolutePath().normalize();
                if (resolved.startsWith(normalizedReportDir)) {
                    return normalizedReportDir.relativize(resolved).toString().replace('\\', '/');
                }
            }
            return resolved.isAbsolute() ? resolved.toUri().toString() : resolved.toString().replace('\\', '/');
        } catch (Exception ignored) {
            return path;
        }
    }

    static Path resolveReportDir() {
        String override = PepeniumConfig.get("PEPENIUM_REPORT_DIR");
        if (override != null) {
            override = override.trim();
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
        return SensitiveDataSanitizer.sanitizeText(root.getMessage().replaceAll("\\s+", " ").trim());
    }

    static String stackTrace(Throwable cause) {
        if (cause == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        return SensitiveDataSanitizer.sanitizeText(writer.toString());
    }

    private static Yaml createSafeYamlParser() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(20);
        options.setNestingDepthLimit(30);
        options.setCodePointLimit(10 * 1024 * 1024);
        return new Yaml(new SafeConstructor(options));
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
        return SensitiveDataSanitizer.sanitizeServerUrl(url);
    }

    private static String uniqueArtifactStem(String prefix) {
        return prefix + "_" + Instant.now().toEpochMilli()
                + "_" + Long.toUnsignedString(System.nanoTime(), 36);
    }
}
