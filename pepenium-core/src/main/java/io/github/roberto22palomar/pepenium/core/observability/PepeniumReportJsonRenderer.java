package io.github.roberto22palomar.pepenium.core.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

final class PepeniumReportJsonRenderer {

    private static final Logger log = LoggerFactory.getLogger(PepeniumReportJsonRenderer.class);

    private PepeniumReportJsonRenderer() {
    }

    static String renderReportJson(PepeniumHtmlReportWriter.ReportContext report, String htmlFileName) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        PepeniumReportSupport.appendJsonField(json, "schemaVersion", 1, true);
        PepeniumReportSupport.appendJsonField(json, "generatedAt", report.finishedAt.toString(), true);
        PepeniumReportSupport.appendJsonField(json, "htmlReport", htmlFileName, true);
        PepeniumReportSupport.appendJsonField(json, "outcome", report.outcome, true);
        PepeniumReportSupport.appendJsonField(json, "testName", report.testName, true);
        PepeniumReportSupport.appendJsonField(json, "profileId", report.profileId, true);
        PepeniumReportSupport.appendJsonField(json, "target", report.target, true);
        PepeniumReportSupport.appendJsonField(json, "driverType", report.driverType, true);
        PepeniumReportSupport.appendJsonField(json, "description", report.description, true);
        PepeniumReportSupport.appendJsonField(json, "sessionId", report.sessionId, true);
        json.append("  \"timing\": {\n");
        PepeniumReportSupport.appendJsonField(json, "startedAt", report.startedAt.toString(), true, 4);
        PepeniumReportSupport.appendJsonField(json, "finishedAt", report.finishedAt.toString(), true, 4);
        PepeniumReportSupport.appendJsonField(json, "durationMillis", report.durationMillis, true, 4);
        PepeniumReportSupport.appendJsonField(json, "durationDisplay", report.duration, false, 4);
        json.append("  },\n");
        json.append("  \"stats\": {\n");
        PepeniumReportSupport.appendJsonField(json, "events", report.totalEvents, true, 4);
        PepeniumReportSupport.appendJsonField(json, "actions", report.actionCount, true, 4);
        PepeniumReportSupport.appendJsonField(json, "waits", report.waitCount, true, 4);
        PepeniumReportSupport.appendJsonField(json, "passedAssertions", report.passedAssertions, true, 4);
        PepeniumReportSupport.appendJsonField(json, "failedAssertions", report.failedAssertions, true, 4);
        PepeniumReportSupport.appendJsonField(json, "screenshots", report.screenshotCount, true, 4);
        PepeniumReportSupport.appendJsonField(json, "errors", report.errorCount, false, 4);
        json.append("  },\n");
        json.append("  \"highlights\": {\n");
        PepeniumReportSupport.appendJsonField(json, "lastStep", report.lastStep, true, 4);
        PepeniumReportSupport.appendJsonField(json, "lastAssertion", report.lastAssertion, true, 4);
        PepeniumReportSupport.appendJsonField(json, "lastScreenshotPath", report.lastScreenshotPath, false, 4);
        json.append("  },\n");
        json.append("  \"execution\": {\n");
        PepeniumReportSupport.appendJsonField(json, "platform", report.deviceContext.platformName, true, 4);
        PepeniumReportSupport.appendJsonField(json, "platformVersion", report.deviceContext.platformVersion, true, 4);
        PepeniumReportSupport.appendJsonField(json, "deviceName", report.deviceContext.deviceName, true, 4);
        PepeniumReportSupport.appendJsonField(json, "browserName", report.deviceContext.browserName, true, 4);
        PepeniumReportSupport.appendJsonField(json, "browserVersion", report.deviceContext.browserVersion, true, 4);
        PepeniumReportSupport.appendJsonField(json, "automationName", report.deviceContext.automationName, true, 4);
        PepeniumReportSupport.appendJsonField(json, "currentUrl", report.currentUrl, true, 4);
        PepeniumReportSupport.appendJsonField(json, "pageTitle", report.pageTitle, true, 4);
        PepeniumReportSupport.appendJsonField(json, "mobileContext", report.mobileContext, true, 4);
        PepeniumReportSupport.appendJsonField(json, "mobilePackage", report.mobilePackage, true, 4);
        PepeniumReportSupport.appendJsonField(json, "mobileActivity", report.mobileActivity, true, 4);
        PepeniumReportSupport.appendJsonField(json, "capabilitiesSummary", report.capabilitiesSummary, false, 4);
        json.append("  },\n");
        json.append("  \"remote\": {\n");
        PepeniumReportSupport.appendJsonField(json, "enabled", report.remoteContext.enabled, true, 4);
        PepeniumReportSupport.appendJsonField(json, "provider", report.remoteContext.provider, true, 4);
        PepeniumReportSupport.appendJsonField(json, "serverHost", report.remoteContext.serverHost, true, 4);
        PepeniumReportSupport.appendJsonField(json, "serverUrl", report.remoteContext.serverUrl, true, 4);
        PepeniumReportSupport.appendJsonField(json, "projectName", report.remoteContext.projectName, true, 4);
        PepeniumReportSupport.appendJsonField(json, "buildName", report.remoteContext.buildName, true, 4);
        PepeniumReportSupport.appendJsonField(json, "sessionName", report.remoteContext.remoteSessionName, true, 4);
        PepeniumReportSupport.appendJsonField(json, "localEnabled", report.remoteContext.localEnabled, false, 4);
        json.append("  },\n");
        json.append("  \"events\": [\n");
        List<PepeniumTimeline.Event> events = report.timelineSnapshot.getEvents();
        for (int i = 0; i < events.size(); i++) {
            PepeniumTimeline.Event event = events.get(i);
            json.append("    {\n");
            PepeniumReportSupport.appendJsonField(json, "time", event.getTime(), true, 6);
            PepeniumReportSupport.appendJsonField(json, "type", event.getType().name(), true, 6);
            PepeniumReportSupport.appendJsonField(json, "status", event.getStatus().name(), true, 6);
            PepeniumReportSupport.appendJsonField(json, "message", event.getMessage(), true, 6);
            PepeniumReportSupport.appendJsonField(json, "screenshotPath", event.getScreenshotPath(), false, 6);
            json.append("    }");
            if (i < events.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");
        json.append("  \"recentSteps\": [\n");
        List<String> steps = report.stepSnapshot.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            json.append("    ").append(PepeniumReportSupport.quoteJson(steps.get(i)));
            if (i < steps.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");
        json.append("  \"failure\": {\n");
        PepeniumReportSupport.appendJsonField(json, "failed", report.failed, true, 4);
        PepeniumReportSupport.appendJsonField(json, "rootType", report.rootType, true, 4);
        PepeniumReportSupport.appendJsonField(json, "rootMessage", report.rootMessage, true, 4);
        PepeniumReportSupport.appendJsonField(json, "stackTrace", report.stackTrace, false, 4);
        json.append("  }\n");
        json.append("}\n");
        return json.toString();
    }

    static PepeniumHtmlReportWriter.ReportSummary loadSummary(Path jsonFile) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = PepeniumReportSupport.YAML.load(Files.readString(jsonFile, StandardCharsets.UTF_8));
            if (data == null) {
                return null;
            }
            Map<String, Object> timing = PepeniumReportSupport.mapValue(data.get("timing"));
            Map<String, Object> stats = PepeniumReportSupport.mapValue(data.get("stats"));
            Map<String, Object> remote = PepeniumReportSupport.mapValue(data.get("remote"));
            return new PepeniumHtmlReportWriter.ReportSummary(
                    PepeniumReportSupport.safe(data.get("testName")),
                    PepeniumReportSupport.safe(data.get("outcome")),
                    PepeniumReportSupport.safe(data.get("profileId")),
                    PepeniumReportSupport.safe(data.get("target")),
                    PepeniumReportSupport.safe(data.get("driverType")),
                    PepeniumReportSupport.safe(data.get("htmlReport")),
                    PepeniumReportSupport.safe(data.get("generatedAt")),
                    PepeniumReportSupport.numberValue(timing.get("durationMillis")),
                    PepeniumReportSupport.safe(timing.get("durationDisplay")),
                    PepeniumReportSupport.numberValue(stats.get("screenshots")),
                    PepeniumReportSupport.safe(remote.get("provider")),
                    PepeniumReportSupport.safe(remote.get("enabled"))
            );
        } catch (Exception e) {
            log.warn("Failed to read Pepenium report summary from '{}': {}", jsonFile, e.getMessage());
            return null;
        }
    }

    static String renderSuiteSummaryJson(List<PepeniumHtmlReportWriter.ReportSummary> summaries) {
        long passed = summaries.stream().filter(summary -> "PASSED".equals(summary.outcome)).count();
        long failed = summaries.size() - passed;
        long totalDuration = summaries.stream().mapToLong(summary -> summary.durationMillis).sum();
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        PepeniumReportSupport.appendJsonField(json, "generatedAt", Instant.now().toString(), true);
        PepeniumReportSupport.appendJsonField(json, "totalReports", summaries.size(), true);
        PepeniumReportSupport.appendJsonField(json, "passed", passed, true);
        PepeniumReportSupport.appendJsonField(json, "failed", failed, true);
        PepeniumReportSupport.appendJsonField(json, "totalDurationMillis", totalDuration, false);
        json.append("}\n");
        return json.toString();
    }
}
