package io.github.roberto22palomar.pepenium.core.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PepeniumReportIndexWriterTest {

    @TempDir
    private Path reportDir;

    @AfterEach
    void tearDown() {
        System.clearProperty("pepenium.report.dir");
        StepTracker.clear();
    }

    @Test
    void writeIndexBuildsSuiteArtifactsAndIgnoresBrokenSummaryFiles() throws Exception {
        System.setProperty("pepenium.report.dir", reportDir.toString());

        StepTracker.record("Open dashboard");
        PepeniumTimeline.recordAssertionPassed("Assert dashboard is visible");
        PepeniumHtmlReportWriter.write("passingReport", null, null);

        StepTracker.clear();
        StepTracker.record("Submit payment");
        PepeniumTimeline.recordAssertionFailed("Assert payment is confirmed");
        PepeniumTimeline.recordScreenshot("Screenshot saved", "C:\\tmp\\payment.png");
        PepeniumHtmlReportWriter.write("failingReport", null, new IllegalStateException("boom"));

        Files.writeString(reportDir.resolve("report-invalid.json"), "{not-valid", StandardCharsets.UTF_8);

        Path indexFile = PepeniumReportIndexWriter.writeIndex(reportDir);
        String indexHtml = Files.readString(indexFile);
        String summaryJson = Files.readString(reportDir.resolve("summary.json"));

        assertTrue(indexHtml.contains("Total Reports"));
        assertTrue(indexHtml.contains("passingReport"));
        assertTrue(indexHtml.contains("failingReport"));
        assertTrue(indexHtml.contains("Open suite summary JSON"));
        assertTrue(summaryJson.contains("\"totalReports\": 2"));
        assertTrue(summaryJson.contains("\"passed\": 1"));
        assertTrue(summaryJson.contains("\"failed\": 1"));
    }
}
