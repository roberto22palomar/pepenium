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
        Path manualScreenshot = Files.write(reportDir.resolve("manual-source.png"), new byte[]{9, 8, 7});
        StepTracker.record("Open login page");
        PepeniumTimeline.recordAction("Submit credentials");
        PepeniumTimeline.recordWait("Wait for secure area");
        PepeniumTimeline.recordWait("Wait for secure area");
        PepeniumTimeline.recordAssertionFailed("Assert secure area is visible");
        PepeniumTimeline.recordScreenshot("Manual screenshot", manualScreenshot.toString());
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
        assertEquals(7, report.totalEvents);
        assertEquals(1, report.failedAssertions);
        assertEquals("Assert secure area is visible", report.lastAssertion);
        assertTrue(report.lastScreenshotPath.contains("screenshots"));
        assertEquals(6, report.eventGroups.size());
        assertEquals(1, report.eventGroups.get(4).screenshots.size());
        assertEquals(4, report.keyEventCount);
        assertEquals(1, report.flowBlocks.size());
        assertEquals(1, report.waitHotspots.size());
        assertEquals("Open login page", report.flowBlocks.get(0).title);
        assertEquals("Submit credentials", report.flowBlocks.get(0).events.get(1).getMessage());
        assertNotNull(report.screenshotUri);
        assertTrue(report.screenshotUri.startsWith("screenshots/"));
        assertTrue(Files.exists(reportDir.resolve(report.screenshotUri)));
        assertTrue(Files.exists(Path.of(report.lastScreenshotPath)));
    }

    @Test
    void htmlReportRendersAllManualScreenshotsAsVisibleArtifacts() throws Exception {
        Path firstScreenshot = Files.write(reportDir.resolve("first-source.png"), new byte[]{4, 5, 6});
        Path secondScreenshot = Files.write(reportDir.resolve("second-source.png"), new byte[]{7, 8, 9});
        StepTracker.record("Open dashboard");
        PepeniumTimeline.recordScreenshot("First screenshot", firstScreenshot.toString());
        StepTracker.record("Open detail");
        PepeniumTimeline.recordScreenshot("Second screenshot", secondScreenshot.toString());

        DriverRequest request = DriverRequest.builder()
                .driverType(DriverType.LOCAL_CHROME)
                .target(TestTarget.WEB_DESKTOP)
                .description("local web")
                .executionProfileId("local-web")
                .capabilities(new MutableCapabilities())
                .build();
        when(driver.getScreenshotAs(OutputType.BYTES)).thenReturn(new byte[]{1, 2, 3});
        when(driver.getSessionId()).thenReturn(new SessionId("session-654321"));
        when(driver.getCurrentUrl()).thenReturn("https://example.test/dashboard");
        when(driver.getTitle()).thenReturn("Dashboard");

        PepeniumHtmlReportWriter.ReportContext report = PepeniumReportCollector.collect(
                "dashboardFlow",
                new DriverSession(driver, request),
                null,
                reportDir
        );

        String html = PepeniumReportHtmlRenderer.render(report);

        assertTrue(html.contains("<h2>Screenshots</h2>"));
        assertTrue(html.contains("Screenshot 1"));
        assertTrue(html.contains("Screenshot 2"));
        assertTrue(html.contains("first-source.png"));
        assertTrue(html.contains("second-source.png"));
    }
}
