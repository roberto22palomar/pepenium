package io.github.roberto22palomar.pepenium.core.observability;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.runtime.DriverSession;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public final class FailureContextReporter {

    private static final Logger log = LoggerFactory.getLogger(FailureContextReporter.class);

    private FailureContextReporter() {
    }

    public static void report(String testName, DriverSession session, Throwable cause) {
        if (session == null || session.getDriver() == null) {
            log.error("Automatic failure context skipped for '{}' because the driver session is not initialized", testName);
            return;
        }

        WebDriver driver = session.getDriver();
        DriverRequest request = session.getRequest();

        log.error("Failure summary for '{}': {} - {}", testName, rootType(cause), rootMessage(cause));
        log.error("Execution: description='{}', target='{}', profile='{}', driver='{}'",
                request.getDescription(),
                request.getTarget(),
                request.getExecutionProfileId(),
                request.getDriverType());
        logSteps();
        log.error("Capabilities: {}", CapabilitiesSummary.summarize(request.getCapabilities()));
        LoggingPreferences.logDetail(log, "Detailed failure stacktrace", cause);

        logScreenshot(driver);
        logSessionId(driver);
        logWebContext(driver);
        logMobileContext(driver, request.getCapabilities());
    }

    private static void logSteps() {
        StepTracker.Snapshot snapshot = StepTracker.snapshot();
        if (snapshot.getSteps().isEmpty()) {
            return;
        }

        if (snapshot.isTruncated()) {
            log.error("Recent steps (showing last {} of {} recorded):",
                    snapshot.getSteps().size(),
                    snapshot.getTotalRecorded());
        } else {
            log.error("Recent steps (showing {}):", snapshot.getSteps().size());
        }

        for (String step : snapshot.getSteps()) {
            log.error("Step: {}", step);
        }
    }

    private static void logScreenshot(WebDriver driver) {
        try {
            if (!(driver instanceof TakesScreenshot)) {
                log.error("Screenshot: not supported by current driver");
                return;
            }

            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path filePath = resolveScreenshotBaseDir().resolve("failure_" + Instant.now().toEpochMilli() + ".png");
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, screenshot);

            log.error("Screenshot: {}", filePath.toAbsolutePath());
        } catch (Exception e) {
            log.error("Screenshot: failed to capture ({})", e.getMessage());
        }
    }

    private static void logSessionId(WebDriver driver) {
        try {
            if (driver instanceof RemoteWebDriver) {
                log.error("Session: {}", ((RemoteWebDriver) driver).getSessionId());
            }
        } catch (Exception e) {
            log.error("Session: unavailable ({})", e.getMessage());
        }
    }

    private static void logWebContext(WebDriver driver) {
        try {
            String url = driver.getCurrentUrl();
            String title = driver.getTitle();

            if (url != null && !url.isBlank()) {
                log.error("Web: url='{}', title='{}'", url, title);
            }
        } catch (Exception e) {
            log.debug("Web context unavailable: {}", e.getMessage());
        }
    }

    private static void logMobileContext(WebDriver driver, Capabilities capabilities) {
        if (!(driver instanceof AppiumDriver)) {
            return;
        }

        AppiumDriver appiumDriver = (AppiumDriver) driver;
        Object currentContext = invokeNoArg(appiumDriver, "getContext");
        if (currentContext == null) {
            currentContext = invokeNoArg(appiumDriver, "getContextHandles");
        }

        try {
            if (currentContext != null) {
                log.error("Mobile: context='{}'", currentContext);
            } else {
                log.error("Mobile: context unavailable");
            }
        } catch (Exception e) {
            log.error("Mobile: context unavailable ({})", e.getMessage());
        }

        if (driver instanceof AndroidDriver) {
            AndroidDriver androidDriver = (AndroidDriver) driver;
            try {
                log.error("Android: package='{}', activity='{}'",
                        androidDriver.getCurrentPackage(),
                        androidDriver.currentActivity());
            } catch (Exception e) {
                log.error("Android: package/activity unavailable ({})", e.getMessage());
            }
            return;
        }

        if (driver instanceof IOSDriver) {
            Object bundleId = capabilities == null ? null : firstCapability(capabilities, "bundleId", "appium:bundleId");
            try {
                log.error("iOS: bundleId='{}', context='{}'",
                        bundleId,
                        currentContext);
            } catch (Exception e) {
                log.error("iOS: bundle/context unavailable ({})", e.getMessage());
            }
        }
    }

    private static Object firstCapability(Capabilities capabilities, String... keys) {
        for (String key : keys) {
            Object value = capabilities.getCapability(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Object invokeNoArg(Object target, String methodName) {
        try {
            return target.getClass().getMethod(methodName).invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Path resolveScreenshotBaseDir() {
        String baseDir = System.getenv("DEVICEFARM_SCREENSHOT_PATH");
        if (baseDir == null || baseDir.isBlank()) {
            baseDir = System.getProperty("java.io.tmpdir");
        }
        return Path.of(baseDir);
    }

    private static String rootType(Throwable cause) {
        Throwable root = rootCause(cause);
        return root == null ? "UnknownError" : root.getClass().getSimpleName();
    }

    private static String rootMessage(Throwable cause) {
        Throwable root = rootCause(cause);
        if (root == null || root.getMessage() == null || root.getMessage().isBlank()) {
            return "No additional error message";
        }
        String message = root.getMessage();
        int buildInfoIndex = message.indexOf("Build info:");
        if (buildInfoIndex >= 0) {
            message = message.substring(0, buildInfoIndex);
        }
        message = message.replaceAll("\\s+", " ").trim();
        return message.length() <= 220 ? message : message.substring(0, 217) + "...";
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
