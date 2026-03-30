package io.github.roberto22palomar.pepenium.core.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

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
        assertTrue(reportJson.contains("\"failure\""));
        assertTrue(reportJson.contains("\"FAIL"));
    }
}
