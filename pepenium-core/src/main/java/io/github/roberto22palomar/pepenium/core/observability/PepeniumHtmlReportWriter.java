package io.github.roberto22palomar.pepenium.core.observability;

import io.github.roberto22palomar.pepenium.core.runtime.DriverSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class PepeniumHtmlReportWriter {

    private static final Logger log = LoggerFactory.getLogger(PepeniumHtmlReportWriter.class);

    private PepeniumHtmlReportWriter() {
    }

    public static void write(String testName, DriverSession session, Throwable cause) {
        try {
            Path reportDir = PepeniumReportSupport.resolveReportDir();
            Files.createDirectories(reportDir);

            ReportContext report = PepeniumReportCollector.collect(testName, session, cause, reportDir);
            String baseFileName = PepeniumReportSupport.buildBaseFileName(report);
            String htmlFileName = baseFileName + ".html";
            Path htmlFile = reportDir.resolve(htmlFileName);
            Path jsonFile = reportDir.resolve(baseFileName + ".json");

            Files.writeString(htmlFile, PepeniumReportHtmlRenderer.render(report), StandardCharsets.UTF_8);
            Files.writeString(jsonFile, PepeniumReportJsonRenderer.renderReportJson(report, htmlFileName), StandardCharsets.UTF_8);

            Path indexFile = PepeniumReportIndexWriter.writeIndex(reportDir);

            log.info("Pepenium HTML report generated at: {}", htmlFile.toAbsolutePath());
            log.info("Pepenium HTML report link: {}", htmlFile.toUri());
            log.info("Pepenium report index: {}", indexFile.toUri());
        } catch (Exception e) {
            log.warn("Failed to write Pepenium HTML report for '{}': {}", testName, e.getMessage());
            LoggingPreferences.logDetail(log, "Detailed report writer failure", e);
        }
    }

    @FunctionalInterface
    interface SummarySelector {
        String get(ReportSummary summary);
    }

    static final class EventGroup {
        final PepeniumTimeline.Event anchorEvent;
        final long previousAnchorEpochMillis;
        final List<PepeniumTimeline.Event> screenshots = new ArrayList<>();

        EventGroup(PepeniumTimeline.Event anchorEvent, long previousAnchorEpochMillis) {
            this.anchorEvent = anchorEvent;
            this.previousAnchorEpochMillis = previousAnchorEpochMillis;
            if (anchorEvent.getType() == PepeniumTimeline.EventType.SCREENSHOT && anchorEvent.getScreenshotPath() != null) {
                this.screenshots.add(anchorEvent);
            }
        }
    }

    static final class RemoteContext {
        final boolean enabled;
        final String provider;
        final String serverHost;
        final String serverUrl;
        final String projectName;
        final String buildName;
        final String remoteSessionName;
        final String localEnabled;
        final String dashboardUrl;

        RemoteContext(
                boolean enabled,
                String provider,
                String serverHost,
                String serverUrl,
                String projectName,
                String buildName,
                String remoteSessionName,
                String localEnabled,
                String dashboardUrl
        ) {
            this.enabled = enabled;
            this.provider = provider;
            this.serverHost = serverHost;
            this.serverUrl = serverUrl;
            this.projectName = projectName;
            this.buildName = buildName;
            this.remoteSessionName = remoteSessionName;
            this.localEnabled = localEnabled;
            this.dashboardUrl = dashboardUrl;
        }

        static RemoteContext disabled() {
            return new RemoteContext(false, null, null, null, null, null, null, null, null);
        }
    }

    static final class DeviceContext {
        final String platformName;
        final String platformVersion;
        final String deviceName;
        final String browserName;
        final String browserVersion;
        final String automationName;

        DeviceContext(
                String platformName,
                String platformVersion,
                String deviceName,
                String browserName,
                String browserVersion,
                String automationName
        ) {
            this.platformName = platformName;
            this.platformVersion = platformVersion;
            this.deviceName = deviceName;
            this.browserName = browserName;
            this.browserVersion = browserVersion;
            this.automationName = automationName;
        }
    }

    static final class ReportSummary {
        final String testName;
        final String outcome;
        final String profileId;
        final String target;
        final String driverType;
        final String htmlReport;
        final String jsonReport;
        final String generatedAt;
        final long durationMillis;
        final String durationDisplay;
        final long screenshotCount;
        final String provider;
        final String remoteEnabled;

        ReportSummary(
                String testName,
                String outcome,
                String profileId,
                String target,
                String driverType,
                String htmlReport,
                String generatedAt,
                long durationMillis,
                String durationDisplay,
                long screenshotCount,
                String provider,
                String remoteEnabled
        ) {
            this.testName = testName;
            this.outcome = outcome;
            this.profileId = profileId;
            this.target = target;
            this.driverType = driverType;
            this.htmlReport = htmlReport;
            this.jsonReport = htmlReport == null ? null : htmlReport.replaceAll("\\.html$", ".json");
            this.generatedAt = generatedAt;
            this.durationMillis = durationMillis;
            this.durationDisplay = durationDisplay;
            this.screenshotCount = screenshotCount;
            this.provider = provider;
            this.remoteEnabled = remoteEnabled;
        }
    }

    static final class ReportContext {
        final Instant startedAt;
        final Instant finishedAt;
        final long durationMillis;
        final String duration;
        final String outcome;
        final String testName;
        final String profileId;
        final String target;
        final String driverType;
        final String description;
        final String sessionId;
        final String currentUrl;
        final String pageTitle;
        final String mobileContext;
        final String mobilePackage;
        final String mobileActivity;
        final DeviceContext deviceContext;
        final String capabilitiesSummary;
        final StepTracker.Snapshot stepSnapshot;
        final PepeniumTimeline.Snapshot timelineSnapshot;
        final List<EventGroup> eventGroups;
        final int totalEvents;
        final int actionCount;
        final int waitCount;
        final int passedAssertions;
        final int failedAssertions;
        final int screenshotCount;
        final int errorCount;
        final String lastStep;
        final String lastAssertion;
        final String lastScreenshotPath;
        final String rootType;
        final String rootMessage;
        final String stackTrace;
        final String screenshotUri;
        final RemoteContext remoteContext;
        final boolean failed;

        ReportContext(
                Instant startedAt,
                Instant finishedAt,
                long durationMillis,
                String duration,
                String outcome,
                String testName,
                String profileId,
                String target,
                String driverType,
                String description,
                String sessionId,
                String currentUrl,
                String pageTitle,
                String mobileContext,
                String mobilePackage,
                String mobileActivity,
                DeviceContext deviceContext,
                String capabilitiesSummary,
                StepTracker.Snapshot stepSnapshot,
                PepeniumTimeline.Snapshot timelineSnapshot,
                List<EventGroup> eventGroups,
                int totalEvents,
                int actionCount,
                int waitCount,
                int passedAssertions,
                int failedAssertions,
                int screenshotCount,
                int errorCount,
                String lastStep,
                String lastAssertion,
                String lastScreenshotPath,
                String rootType,
                String rootMessage,
                String stackTrace,
                String screenshotUri,
                RemoteContext remoteContext,
                boolean failed
        ) {
            this.startedAt = startedAt;
            this.finishedAt = finishedAt;
            this.durationMillis = durationMillis;
            this.duration = duration;
            this.outcome = outcome;
            this.testName = testName;
            this.profileId = profileId;
            this.target = target;
            this.driverType = driverType;
            this.description = description;
            this.sessionId = sessionId;
            this.currentUrl = currentUrl;
            this.pageTitle = pageTitle;
            this.mobileContext = mobileContext;
            this.mobilePackage = mobilePackage;
            this.mobileActivity = mobileActivity;
            this.deviceContext = deviceContext;
            this.capabilitiesSummary = capabilitiesSummary;
            this.stepSnapshot = stepSnapshot;
            this.timelineSnapshot = timelineSnapshot;
            this.eventGroups = eventGroups;
            this.totalEvents = totalEvents;
            this.actionCount = actionCount;
            this.waitCount = waitCount;
            this.passedAssertions = passedAssertions;
            this.failedAssertions = failedAssertions;
            this.screenshotCount = screenshotCount;
            this.errorCount = errorCount;
            this.lastStep = lastStep;
            this.lastAssertion = lastAssertion;
            this.lastScreenshotPath = lastScreenshotPath;
            this.rootType = rootType;
            this.rootMessage = rootMessage;
            this.stackTrace = stackTrace;
            this.screenshotUri = screenshotUri;
            this.remoteContext = remoteContext;
            this.failed = failed;
        }
    }
}
