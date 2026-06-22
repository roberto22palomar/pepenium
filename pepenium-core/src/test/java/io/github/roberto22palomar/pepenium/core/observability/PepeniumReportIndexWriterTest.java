package io.github.roberto22palomar.pepenium.core.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void concurrentReportsProduceCompleteReadableSuiteArtifacts() throws Exception {
        System.setProperty("pepenium.report.dir", reportDir.toString());
        int reportCount = 24;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> reports = new ArrayList<>();

        try {
            for (int index = 0; index < reportCount; index++) {
                final int reportIndex = index;
                reports.add(executor.submit(() -> {
                    start.await();
                    PepeniumHtmlReportWriter.write("parallelReport" + reportIndex, null, null);
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> report : reports) {
                report.get();
            }
        } finally {
            executor.shutdownNow();
        }

        String summaryJson = Files.readString(reportDir.resolve("summary.json"));
        String indexHtml = Files.readString(reportDir.resolve("index.html"));
        long jsonReports;
        try (java.util.stream.Stream<Path> files = Files.list(reportDir)) {
            jsonReports = files
                    .filter(path -> path.getFileName().toString().startsWith("report-"))
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .count();
        }

        assertEquals(reportCount, jsonReports);
        assertTrue(summaryJson.contains("\"totalReports\": " + reportCount));
        assertTrue(indexHtml.contains(reportCount + " report(s) visible"));
        for (int index = 0; index < reportCount; index++) {
            assertTrue(indexHtml.contains("parallelReport" + index));
        }
    }
}
