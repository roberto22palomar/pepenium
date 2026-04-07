package io.github.roberto22palomar.pepenium.core.observability;

import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.runtime.DriverSession;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class PepeniumReportCollector {

    private PepeniumReportCollector() {
    }

    static PepeniumHtmlReportWriter.ReportContext collect(String testName, DriverSession session, Throwable cause, Path reportDir) {
        Instant finishedAt = Instant.now();
        StepTracker.Snapshot stepSnapshot = StepTracker.snapshot();
        PepeniumTimeline.Snapshot timelineSnapshot = PepeniumTimeline.snapshot();
        DriverRequest request = session == null ? null : session.getRequest();
        WebDriver driver = session == null ? null : session.getDriver();
        String outcome = cause == null ? "PASSED" : "FAILED";
        Instant startedAt = timelineSnapshot.getStartedAt() == null ? finishedAt : timelineSnapshot.getStartedAt();
        PepeniumHtmlReportWriter.RemoteContext remoteContext = resolveRemoteContext(request, driver);
        PepeniumHtmlReportWriter.DeviceContext deviceContext =
                resolveDeviceContext(request == null ? null : request.getCapabilities());

        return new PepeniumHtmlReportWriter.ReportContext(
                startedAt,
                finishedAt,
                Duration.between(startedAt, finishedAt).toMillis(),
                PepeniumReportSupport.formatDuration(startedAt, finishedAt),
                outcome,
                PepeniumReportSupport.safe(testName),
                request == null ? null : PepeniumReportSupport.safe(request.getExecutionProfileId()),
                request == null ? null : PepeniumReportSupport.safe(request.getTarget()),
                request == null ? null : PepeniumReportSupport.safe(request.getDriverType()),
                request == null ? null : PepeniumReportSupport.safe(request.getDescription()),
                PepeniumReportSupport.safe(PepeniumReportSupport.sessionId(driver)),
                PepeniumReportSupport.safe(PepeniumReportSupport.currentUrl(driver)),
                PepeniumReportSupport.safe(PepeniumReportSupport.pageTitle(driver)),
                PepeniumReportSupport.safe(PepeniumReportSupport.mobileContext(driver)),
                PepeniumReportSupport.safe(PepeniumReportSupport.mobilePackage(driver)),
                PepeniumReportSupport.safe(PepeniumReportSupport.mobileActivity(driver)),
                deviceContext,
                PepeniumReportSupport.safe(CapabilitiesSummary.summarize(request == null ? null : request.getCapabilities())),
                stepSnapshot,
                timelineSnapshot,
                buildEventGroups(timelineSnapshot),
                buildFlowBlocks(timelineSnapshot),
                findWaitHotspots(timelineSnapshot),
                countKeyEvents(timelineSnapshot),
                countEvents(timelineSnapshot, null, null),
                countEvents(timelineSnapshot, PepeniumTimeline.EventType.ACTION, null),
                countEvents(timelineSnapshot, PepeniumTimeline.EventType.WAIT, null),
                countEvents(timelineSnapshot, PepeniumTimeline.EventType.ASSERT, PepeniumTimeline.EventStatus.PASSED),
                countEvents(timelineSnapshot, PepeniumTimeline.EventType.ASSERT, PepeniumTimeline.EventStatus.FAILED),
                countEvents(timelineSnapshot, PepeniumTimeline.EventType.SCREENSHOT, null),
                countEvents(timelineSnapshot, PepeniumTimeline.EventType.ERROR, null),
                findLastMessage(timelineSnapshot, PepeniumTimeline.EventType.STEP),
                findLastMessage(timelineSnapshot, PepeniumTimeline.EventType.ASSERT),
                findLastScreenshotPath(timelineSnapshot),
                PepeniumReportSupport.safe(PepeniumReportSupport.rootType(cause)),
                PepeniumReportSupport.safe(PepeniumReportSupport.rootMessage(cause)),
                PepeniumReportSupport.safe(PepeniumReportSupport.stackTrace(cause)),
                PepeniumReportSupport.screenshotUri(driver, reportDir),
                remoteContext,
                cause != null
        );
    }

    static PepeniumHtmlReportWriter.DeviceContext resolveDeviceContext(Capabilities capabilities) {
        return new PepeniumHtmlReportWriter.DeviceContext(
                PepeniumReportSupport.firstCapability(capabilities, "platformName", "appium:platformName"),
                PepeniumReportSupport.firstCapability(capabilities, "platformVersion", "appium:platformVersion", "osVersion"),
                PepeniumReportSupport.firstCapability(capabilities, "deviceName", "appium:deviceName"),
                PepeniumReportSupport.firstCapability(capabilities, "browserName"),
                PepeniumReportSupport.firstCapability(capabilities, "browserVersion"),
                PepeniumReportSupport.firstCapability(capabilities, "automationName", "appium:automationName")
        );
    }

    static List<PepeniumHtmlReportWriter.EventGroup> buildEventGroups(PepeniumTimeline.Snapshot snapshot) {
        List<PepeniumHtmlReportWriter.EventGroup> groups = new ArrayList<>();
        PepeniumHtmlReportWriter.EventGroup current = null;
        long previousAnchorEpochMillis = -1L;
        for (PepeniumTimeline.Event event : snapshot.getEvents()) {
            if (event.getType() == PepeniumTimeline.EventType.SCREENSHOT && event.getScreenshotPath() != null) {
                if (current == null) {
                    current = new PepeniumHtmlReportWriter.EventGroup(event, previousAnchorEpochMillis);
                    groups.add(current);
                    previousAnchorEpochMillis = event.getEpochMillis();
                } else {
                    current.screenshots.add(event);
                }
                continue;
            }
            current = new PepeniumHtmlReportWriter.EventGroup(event, previousAnchorEpochMillis);
            groups.add(current);
            previousAnchorEpochMillis = event.getEpochMillis();
        }
        return groups;
    }

    static List<PepeniumHtmlReportWriter.FlowBlock> buildFlowBlocks(PepeniumTimeline.Snapshot snapshot) {
        List<PepeniumHtmlReportWriter.FlowBlock> blocks = new ArrayList<>();
        List<PepeniumTimeline.Event> currentEvents = new ArrayList<>();
        String currentTitle = null;
        String currentTime = null;
        long currentStarted = -1L;

        for (PepeniumTimeline.Event event : snapshot.getEvents()) {
            if (event.getType() == PepeniumTimeline.EventType.STEP && !currentEvents.isEmpty()) {
                blocks.add(toFlowBlock(currentTitle, currentTime, currentStarted, currentEvents));
                currentEvents = new ArrayList<>();
                currentTitle = null;
                currentTime = null;
                currentStarted = -1L;
            }

            if (currentEvents.isEmpty()) {
                currentStarted = event.getEpochMillis();
                currentTime = event.getTime();
            }
            if (currentTitle == null) {
                currentTitle = event.getType() == PepeniumTimeline.EventType.STEP
                        ? event.getMessage()
                        : defaultBlockTitle(event);
            }
            currentEvents.add(event);
        }

        if (!currentEvents.isEmpty()) {
            blocks.add(toFlowBlock(currentTitle, currentTime, currentStarted, currentEvents));
        }
        return blocks;
    }

    static List<PepeniumHtmlReportWriter.WaitHotspot> findWaitHotspots(PepeniumTimeline.Snapshot snapshot) {
        Map<String, WaitCounter> counters = new LinkedHashMap<>();
        for (PepeniumTimeline.Event event : snapshot.getEvents()) {
            if (event.getType() != PepeniumTimeline.EventType.WAIT) {
                continue;
            }
            WaitCounter counter = counters.computeIfAbsent(event.getMessage(),
                    ignored -> new WaitCounter(event.getTime(), event.getTime()));
            counter.count++;
            counter.lastSeen = event.getTime();
        }
        List<PepeniumHtmlReportWriter.WaitHotspot> hotspots = new ArrayList<>();
        counters.forEach((message, counter) -> {
            if (counter.count > 1) {
                hotspots.add(new PepeniumHtmlReportWriter.WaitHotspot(
                        message,
                        counter.count,
                        counter.firstSeen,
                        counter.lastSeen
                ));
            }
        });
        hotspots.sort(Comparator.comparingInt((PepeniumHtmlReportWriter.WaitHotspot item) -> item.count).reversed());
        return hotspots.size() <= 5 ? hotspots : hotspots.subList(0, 5);
    }

    static int countKeyEvents(PepeniumTimeline.Snapshot snapshot) {
        int total = 0;
        for (PepeniumTimeline.Event event : snapshot.getEvents()) {
            if (event.getType() == PepeniumTimeline.EventType.STEP
                    || event.getType() == PepeniumTimeline.EventType.ASSERT
                    || event.getType() == PepeniumTimeline.EventType.ERROR
                    || event.getType() == PepeniumTimeline.EventType.SCREENSHOT) {
                total++;
            }
        }
        return total;
    }

    static int countEvents(
            PepeniumTimeline.Snapshot snapshot,
            PepeniumTimeline.EventType type,
            PepeniumTimeline.EventStatus status
    ) {
        int count = 0;
        for (PepeniumTimeline.Event event : snapshot.getEvents()) {
            boolean typeMatches = type == null || event.getType() == type;
            boolean statusMatches = status == null || event.getStatus() == status;
            if (typeMatches && statusMatches) {
                count++;
            }
        }
        return count;
    }

    static String findLastMessage(PepeniumTimeline.Snapshot snapshot, PepeniumTimeline.EventType type) {
        List<PepeniumTimeline.Event> events = snapshot.getEvents();
        for (int i = events.size() - 1; i >= 0; i--) {
            PepeniumTimeline.Event event = events.get(i);
            if (event.getType() == type) {
                return event.getMessage();
            }
        }
        return null;
    }

    static String findLastScreenshotPath(PepeniumTimeline.Snapshot snapshot) {
        List<PepeniumTimeline.Event> events = snapshot.getEvents();
        for (int i = events.size() - 1; i >= 0; i--) {
            PepeniumTimeline.Event event = events.get(i);
            if (event.getType() == PepeniumTimeline.EventType.SCREENSHOT && event.getScreenshotPath() != null) {
                return event.getScreenshotPath();
            }
        }
        return null;
    }

    private static PepeniumHtmlReportWriter.FlowBlock toFlowBlock(String title,
                                                                  String startedAt,
                                                                  long startedEpochMillis,
                                                                  List<PepeniumTimeline.Event> events) {
        int actionCount = 0;
        int waitCount = 0;
        int assertionCount = 0;
        int screenshotCount = 0;
        int errorCount = 0;
        boolean failed = false;
        String leadScreenshotPath = null;
        long finishedEpochMillis = startedEpochMillis;

        for (PepeniumTimeline.Event event : events) {
            finishedEpochMillis = event.getEpochMillis();
            switch (event.getType()) {
                case ACTION:
                    actionCount++;
                    break;
                case WAIT:
                    waitCount++;
                    break;
                case ASSERT:
                    assertionCount++;
                    break;
                case SCREENSHOT:
                    screenshotCount++;
                    if (leadScreenshotPath == null && event.getScreenshotPath() != null) {
                        leadScreenshotPath = event.getScreenshotPath();
                    }
                    break;
                case ERROR:
                    errorCount++;
                    break;
                case STEP:
                default:
                    break;
            }
            if (event.getStatus() == PepeniumTimeline.EventStatus.FAILED
                    || event.getType() == PepeniumTimeline.EventType.ERROR) {
                failed = true;
            }
        }

        return new PepeniumHtmlReportWriter.FlowBlock(
                title,
                startedAt,
                startedEpochMillis,
                finishedEpochMillis,
                events,
                actionCount,
                waitCount,
                assertionCount,
                screenshotCount,
                errorCount,
                failed,
                leadScreenshotPath
        );
    }

    private static String defaultBlockTitle(PepeniumTimeline.Event event) {
        switch (event.getType()) {
            case ASSERT:
                return "Assertion checkpoint";
            case ACTION:
                return "Action sequence";
            case WAIT:
                return "Wait sequence";
            case SCREENSHOT:
                return "Screenshot evidence";
            case ERROR:
                return "Failure event";
            case STEP:
            default:
                return event.getMessage();
        }
    }

    static PepeniumHtmlReportWriter.RemoteContext resolveRemoteContext(DriverRequest request, WebDriver driver) {
        if (request == null) {
            return PepeniumHtmlReportWriter.RemoteContext.disabled();
        }

        Capabilities capabilities = request.getCapabilities();
        String host = request.getServerUrl() == null ? null : request.getServerUrl().getHost();
        String sanitizedUrl = PepeniumReportSupport.sanitizeServerUrl(request.getServerUrl());
        String driverType = request.getDriverType() == null ? null : request.getDriverType().name();
        String provider = detectProvider(host, driverType);
        String projectName = PepeniumReportSupport.capabilityString(capabilities, "bstack:options", "projectName");
        String buildName = PepeniumReportSupport.capabilityString(capabilities, "bstack:options", "buildName");
        String sessionName = PepeniumReportSupport.capabilityString(capabilities, "bstack:options", "sessionName");
        String localFlag = PepeniumReportSupport.capabilityString(capabilities, "bstack:options", "local");
        if (localFlag == null) {
            localFlag = PepeniumReportSupport.capabilityString(capabilities, "browserstack.local", null);
        }

        if (driver instanceof RemoteWebDriver || request.getServerUrl() != null) {
            return new PepeniumHtmlReportWriter.RemoteContext(
                    true,
                    provider,
                    host,
                    sanitizedUrl,
                    projectName,
                    buildName,
                    sessionName,
                    localFlag,
                    remoteDashboardUrl(provider)
            );
        }
        return PepeniumHtmlReportWriter.RemoteContext.disabled();
    }

    private static String remoteDashboardUrl(String provider) {
        if ("BrowserStack".equals(provider)) {
            return "https://automate.browserstack.com/dashboard/v2";
        }
        return null;
    }

    private static String detectProvider(String host, String driverType) {
        if (host != null && host.contains("browserstack")) {
            return "BrowserStack";
        }
        if (host != null && host.contains("amazonaws")) {
            return "AWS Device Farm";
        }
        if (host != null) {
            return "Remote";
        }
        return driverType != null && driverType.contains("LOCAL") ? "Local" : "Unknown";
    }

    private static final class WaitCounter {
        private int count;
        private final String firstSeen;
        private String lastSeen;

        private WaitCounter(String firstSeen, String lastSeen) {
            this.firstSeen = firstSeen;
            this.lastSeen = lastSeen;
        }
    }
}
