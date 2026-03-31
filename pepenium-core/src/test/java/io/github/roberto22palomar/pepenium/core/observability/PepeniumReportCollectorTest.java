package io.github.roberto22palomar.pepenium.core.observability;

import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.DriverSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PepeniumReportCollectorTest {

    @Mock
    private RemoteWebDriver driver;

    @TempDir
    private Path reportDir;

    @AfterEach
    void tearDown() {
        StepTracker.clear();
    }

    @Test
    void collectCapturesRemoteContextTimelineAndScreenshotArtifacts() throws Exception {
        StepTracker.record("Open login page");
        PepeniumTimeline.recordAction("Submit credentials");
        PepeniumTimeline.recordAssertionFailed("Assert secure area is visible");
        PepeniumTimeline.recordScreenshot("Manual screenshot", "C:\\tmp\\manual.png");
        PepeniumTimeline.recordError("Remote session failed");

        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("appium:deviceName", "Pixel 8");
        capabilities.setCapability("browserName", "chrome");
        capabilities.setCapability("bstack:options", Map.of(
                "projectName", "Checkout",
                "buildName", "Build 17",
                "sessionName", "login-flow",
                "local", "true"
        ));
        DriverRequest request = DriverRequest.builder()
                .driverType(DriverType.REMOTE_WEB)
                .target(TestTarget.WEB_DESKTOP)
                .description("checkout login")
                .executionProfileId("browserstack-web")
                .serverUrl(new URL("https://hub.browserstack.com/wd/hub"))
                .capabilities(capabilities)
                .build();
        when(driver.getScreenshotAs(OutputType.BYTES)).thenReturn(new byte[]{1, 2, 3});
        when(driver.getSessionId()).thenReturn(new SessionId("session-123456"));
        when(driver.getCurrentUrl()).thenReturn("https://example.test/login");
        when(driver.getTitle()).thenReturn("Login");

        PepeniumHtmlReportWriter.ReportContext report = PepeniumReportCollector.collect(
                "loginFlow",
                new DriverSession(driver, request),
                new IllegalStateException("Screen mismatch"),
                reportDir
        );

        assertEquals("FAILED", report.outcome);
        assertEquals("browserstack-web", report.profileId);
        assertEquals("Pixel 8", report.deviceContext.deviceName);
        assertEquals("BrowserStack", report.remoteContext.provider);
        assertTrue(report.remoteContext.enabled);
        assertEquals(5, report.totalEvents);
        assertEquals(1, report.failedAssertions);
        assertEquals("Assert secure area is visible", report.lastAssertion);
        assertEquals("C:\\tmp\\manual.png", report.lastScreenshotPath);
        assertEquals(4, report.eventGroups.size());
        assertEquals(1, report.eventGroups.get(2).screenshots.size());
        assertNotNull(report.screenshotUri);
        assertTrue(Files.exists(Path.of(URI.create(report.screenshotUri))));
    }
}
