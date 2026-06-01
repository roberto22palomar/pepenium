package io.github.roberto22palomar.pepenium.core.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PepeniumHtmlReportWriterTest {

    @AfterEach
    void clearProperties() {
        System.clearProperty("pepenium.report.dir");
        StepTracker.clear();
    }

    @Test
    void writesHtmlReportAndIndexForPassingTest() throws Exception {
        Path reportDir = Files.createTempDirectory("pepenium-report-test");
        System.setProperty("pepenium.report.dir", reportDir.toString());
        StepTracker.record("Open login page");
        StepTracker.record("Validate login button");
        PepeniumTimeline.recordAssertionPassed("Assert login form is visible");

        PepeniumHtmlReportWriter.write("samplePassingTest", null, null);

        assertTrue(Files.exists(reportDir.resolve("index.html")));
        assertTrue(Files.exists(reportDir.resolve("summary.json")));
        Path reportFile = Files.list(reportDir)
                .filter(path -> path.getFileName().toString().startsWith("report-"))
                .filter(path -> path.getFileName().toString().endsWith(".html"))
                .findFirst()
                .orElseThrow();
        Path jsonFile = Files.list(reportDir)
                .filter(path -> path.getFileName().toString().startsWith("report-"))
                .filter(path -> path.getFileName().toString().endsWith(".json"))
                .findFirst()
                .orElseThrow();

        String reportHtml = Files.readString(reportFile);
        String indexHtml = Files.readString(reportDir.resolve("index.html"));
        String reportJson = Files.readString(jsonFile);

        assertTrue(reportHtml.contains("Duration"));
        assertTrue(reportHtml.contains("Highlights"));
        assertTrue(reportHtml.contains("Last Step"));
        assertTrue(reportHtml.contains("Last Assertion"));
        assertTrue(reportHtml.contains("Visual Summary"));
        assertTrue(reportHtml.contains("Execution Blocks"));
        assertTrue(reportHtml.contains("Key Timeline"));
        assertTrue(reportHtml.contains("Wait Hotspots"));
        assertTrue(reportHtml.contains("Timeline"));
        assertTrue(reportHtml.contains("PASS"));
        assertTrue(indexHtml.contains("Total Reports"));
        assertTrue(indexHtml.contains("Passed"));
        assertTrue(indexHtml.contains("Open suite summary JSON"));
        assertTrue(reportJson.contains("\"stats\""));
        assertTrue(reportJson.contains("\"events\""));
    }

    @Test
    void writesFailureDetailsForFailedTest() throws Exception {
        Path reportDir = Files.createTempDirectory("pepenium-report-failure-test");
        System.setProperty("pepenium.report.dir", reportDir.toString());
        StepTracker.record("Submit login form");
        PepeniumTimeline.recordAssertionFailed("Assert secure area is visible");
        PepeniumTimeline.recordScreenshot("Screenshot saved", "C:\\tmp\\sample.png");

        PepeniumHtmlReportWriter.write("sampleFailingTest", null, new IllegalStateException("Boom"));

        Path reportFile = Files.list(reportDir)
                .filter(path -> path.getFileName().toString().startsWith("report-"))
                .filter(path -> path.getFileName().toString().endsWith(".html"))
                .findFirst()
                .orElseThrow();
        Path jsonFile = Files.list(reportDir)
                .filter(path -> path.getFileName().toString().startsWith("report-"))
                .filter(path -> path.getFileName().toString().endsWith(".json"))
                .findFirst()
                .orElseThrow();

        String reportHtml = Files.readString(reportFile);
        String reportJson = Files.readString(jsonFile);
        assertTrue(reportHtml.contains("FAILED"));
        assertTrue(reportHtml.contains("Boom"));
        assertTrue(reportHtml.contains("Submit login form"));
        assertTrue(reportHtml.contains("Timeline"));
        assertTrue(reportHtml.contains("Remote Session") || reportHtml.contains("Execution Context"));
        assertTrue(reportHtml.contains("Screenshot saved"));
        assertTrue(reportHtml.contains("ASSERT"));
        assertTrue(reportHtml.contains("FAIL"));
        assertTrue(reportHtml.contains("Assert secure area is visible"));
        assertTrue(reportHtml.contains("Last step before failure"));
        assertTrue(reportHtml.contains("Open raw timeline"));
        assertTrue(reportJson.contains("\"failure\""));
        assertTrue(reportJson.contains("\"FAIL"));
    }

    @Test
    void rendersScreenshotHrefRelativeToReportDirectory() {
        Path reportDir = Path.of("target", "pepenium-reports");
        String href = PepeniumReportSupport.pathToHref(
                "target/pepenium-reports/screenshots/manual_123_screenshot.png",
                reportDir
        );

        assertTrue(href.equals("screenshots/manual_123_screenshot.png"));
    }

    @Test
    void linksScreenshotsFromExecutionBlocksAndTimeline() throws Exception {
        Path reportDir = Files.createTempDirectory("pepenium-report-screenshot-test");
        Path screenshotDir = reportDir.resolve("screenshots");
        Files.createDirectories(screenshotDir);
        Path screenshot = screenshotDir.resolve("captured-step.png");
        Files.write(screenshot, new byte[]{1, 2, 3});

        System.setProperty("pepenium.report.dir", reportDir.toString());
        StepTracker.record("Capture dashboard evidence");
        PepeniumTimeline.recordScreenshot("Dashboard after load", screenshot.toString());

        PepeniumHtmlReportWriter.write("sampleScreenshotTest", null, null);

        Path reportFile = Files.list(reportDir)
                .filter(path -> path.getFileName().toString().startsWith("report-"))
                .filter(path -> path.getFileName().toString().endsWith(".html"))
                .findFirst()
                .orElseThrow();
        String reportHtml = Files.readString(reportFile);

        assertTrue(reportHtml.contains("Screenshots (1)"));
        assertTrue(reportHtml.contains("Dashboard after load"));
        assertTrue(reportHtml.contains("screenshots/"));
    }

    @Test
    void indexOnlyShowsReportsFromCurrentExecution() throws Exception {
        Path reportDir = Files.createTempDirectory("pepenium-report-current-run-test");
        System.setProperty("pepenium.report.dir", reportDir.toString());
        Files.writeString(reportDir.resolve("report-old.json"), oldReportJson(), java.nio.charset.StandardCharsets.UTF_8);

        StepTracker.record("Open current page");
        PepeniumHtmlReportWriter.write("currentExecutionTest", null, null);

        String indexHtml = Files.readString(reportDir.resolve("index.html"));
        String summaryJson = Files.readString(reportDir.resolve("summary.json"));

        assertTrue(Files.exists(reportDir.resolve(PepeniumReportRun.indexFileName())));
        assertTrue(indexHtml.contains("Pepenium Current Execution"));
        assertTrue(indexHtml.contains("currentExecutionTest"));
        assertTrue(indexHtml.contains(PepeniumReportRun.indexFileName()));
        assertFalse(indexHtml.contains("oldExecutionTest"));
        assertTrue(summaryJson.contains("\"totalReports\": 1"));
    }

    private static String oldReportJson() {
        return "{\n"
                + "  \"schemaVersion\": 1,\n"
                + "  \"generatedAt\": \"2026-01-01T00:00:00Z\",\n"
                + "  \"runId\": \"old-run\",\n"
                + "  \"runStartedAt\": \"2026-01-01T00:00:00Z\",\n"
                + "  \"htmlReport\": \"report-old.html\",\n"
                + "  \"outcome\": \"PASSED\",\n"
                + "  \"testName\": \"oldExecutionTest\",\n"
                + "  \"profileId\": \"local-web\",\n"
                + "  \"target\": \"WEB_DESKTOP\",\n"
                + "  \"driverType\": \"LOCAL_CHROME\",\n"
                + "  \"timing\": {\"durationMillis\": 1, \"durationDisplay\": \"0s 001ms\"},\n"
                + "  \"stats\": {\"screenshots\": 0},\n"
                + "  \"remote\": {\"provider\": \"local\", \"enabled\": false}\n"
                + "}\n";
    }
}
