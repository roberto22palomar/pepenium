package io.github.roberto22palomar.pepenium.core.observability;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.DriverSession;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FailureContextReporterTest {

    @Mock
    private RemoteWebDriver remoteDriver;

    @Mock
    private AndroidDriver androidDriver;

    @Mock
    private IOSDriver iosDriver;

    @TempDir
    private Path tempDir;

    private org.apache.logging.log4j.core.Logger logger;
    private MemoryAppender appender;
    private String previousTmpDir;

    @BeforeEach
    void setUp() {
        previousTmpDir = System.getProperty("java.io.tmpdir");
        logger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger(FailureContextReporter.class);
        appender = new MemoryAppender("failure-context-test");
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        logger.removeAppender(appender);
        appender.stop();
        if (previousTmpDir == null) {
            System.clearProperty("java.io.tmpdir");
        } else {
            System.setProperty("java.io.tmpdir", previousTmpDir);
        }
        System.clearProperty("pepenium.detail.logging");
        StepTracker.clear();
    }

    @Test
    void reportSkipsWhenSessionIsMissing() {
        FailureContextReporter.report("missingSession", null, new RuntimeException("boom"));

        assertTrue(appender.contains("Automatic failure context skipped for 'missingSession'"));
    }

    @Test
    void reportWritesScreenshotAndLogsWebContext() throws Exception {
        System.setProperty("java.io.tmpdir", tempDir.toString());
        StepTracker.record("Open login page");
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("browserName", "chrome");
        DriverRequest request = DriverRequest.builder()
                .driverType(DriverType.REMOTE_WEB)
                .description("remote web")
                .target(TestTarget.WEB_DESKTOP)
                .executionProfileId("local-web")
                .capabilities(capabilities)
                .build();
        when(remoteDriver.getScreenshotAs(OutputType.BYTES)).thenReturn(new byte[]{1, 2, 3});
        when(remoteDriver.getSessionId()).thenReturn(new SessionId("abcdef123456"));
        when(remoteDriver.getCurrentUrl()).thenReturn("https://example.test/login");
        when(remoteDriver.getTitle()).thenReturn("Example");

        FailureContextReporter.report(
                "webFailure",
                new DriverSession(remoteDriver, request),
                new RuntimeException("wrapper", new IllegalArgumentException("Broken field Build info: ignored"))
        );

        assertTrue(Files.list(tempDir).anyMatch(path -> path.getFileName().toString().startsWith("failure_")));
        assertTrue(appender.contains("Failure summary for 'webFailure': IllegalArgumentException - Broken field"));
        assertTrue(appender.contains("Recent steps"));
        assertTrue(appender.contains("Step:"));
        assertTrue(appender.contains("Capabilities: browserName=chrome"));
        assertTrue(appender.contains("Session: abcdef123456"));
        assertTrue(appender.contains("Web: url='https://example.test/login', title='Example'"));
    }

    @Test
    void reportLogsAndroidContext() {
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("platformName", "Android");
        DriverRequest request = DriverRequest.builder()
                .driverType(DriverType.ANDROID_APPIUM)
                .description("android app")
                .target(TestTarget.ANDROID_NATIVE)
                .capabilities(capabilities)
                .build();
        when(androidDriver.getScreenshotAs(OutputType.BYTES)).thenReturn(new byte[]{1});
        when(androidDriver.getSessionId()).thenReturn(new SessionId("android-session"));
        when(androidDriver.getContext()).thenReturn("NATIVE_APP");
        when(androidDriver.getCurrentPackage()).thenReturn("io.github.pepenium");
        when(androidDriver.currentActivity()).thenReturn(".MainActivity");

        FailureContextReporter.report(
                "androidFailure",
                new DriverSession(androidDriver, request),
                new IllegalStateException("android boom")
        );

        assertTrue(appender.contains("Mobile: context='NATIVE_APP'"));
        assertTrue(appender.contains("Android: package='io.github.pepenium', activity='.MainActivity'"));
    }

    @Test
    void reportLogsIosContext() {
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("appium:bundleId", "io.github.pepenium.ios");
        DriverRequest request = DriverRequest.builder()
                .driverType(DriverType.IOS_APPIUM)
                .description("ios app")
                .target(TestTarget.IOS_NATIVE)
                .capabilities(capabilities)
                .build();
        when(iosDriver.getScreenshotAs(OutputType.BYTES)).thenReturn(new byte[]{1});
        when(iosDriver.getSessionId()).thenReturn(new SessionId("ios-session"));
        when(iosDriver.getContext()).thenReturn("WEBVIEW_1");

        FailureContextReporter.report(
                "iosFailure",
                new DriverSession(iosDriver, request),
                new IllegalStateException("ios boom")
        );

        assertTrue(appender.contains("Mobile: context='WEBVIEW_1'"));
        assertTrue(appender.contains("iOS: bundleId='io.github.pepenium.ios', context='WEBVIEW_1'"));
    }

    private static final class MemoryAppender extends AbstractAppender {
        private final List<String> messages = new ArrayList<>();

        private MemoryAppender(String name) {
            super(name, null, null, false, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }

        private boolean contains(String fragment) {
            return messages.stream().anyMatch(message -> message.contains(fragment));
        }
    }
}
