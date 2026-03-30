package io.github.roberto22palomar.pepenium.core.observability;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.runtime.DriverSession;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PepeniumHtmlReportWriter {

    private static final Logger log = LoggerFactory.getLogger(PepeniumHtmlReportWriter.class);
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Yaml YAML = new Yaml();

    private PepeniumHtmlReportWriter() {
    }

    public static void write(String testName, DriverSession session, Throwable cause) {
        try {
            Path reportDir = resolveReportDir();
            Files.createDirectories(reportDir);

            ReportContext report = collect(testName, session, cause, reportDir);
            String baseFileName = buildBaseFileName(report);
            Path htmlFile = reportDir.resolve(baseFileName + ".html");
            Path jsonFile = reportDir.resolve(baseFileName + ".json");

            Files.writeString(htmlFile, renderReport(report), StandardCharsets.UTF_8);
            Files.writeString(jsonFile, renderReportJson(report, htmlFile.getFileName().toString()), StandardCharsets.UTF_8);

            Path indexFile = writeIndex(reportDir);

            log.info("Pepenium HTML report generated at: {}", htmlFile.toAbsolutePath());
            log.info("Pepenium HTML report link: {}", htmlFile.toUri());
            log.info("Pepenium report index: {}", indexFile.toUri());
        } catch (Exception e) {
            log.warn("Failed to write Pepenium HTML report for '{}': {}", testName, e.getMessage());
            LoggingPreferences.logDetail(log, "Detailed report writer failure", e);
        }
    }

    private static ReportContext collect(String testName, DriverSession session, Throwable cause, Path reportDir) {
        Instant finishedAt = Instant.now();
        StepTracker.Snapshot stepSnapshot = StepTracker.snapshot();
        PepeniumTimeline.Snapshot timelineSnapshot = PepeniumTimeline.snapshot();
        DriverRequest request = session == null ? null : session.getRequest();
        WebDriver driver = session == null ? null : session.getDriver();
        String outcome = cause == null ? "PASSED" : "FAILED";
        Instant startedAt = timelineSnapshot.getStartedAt() == null ? finishedAt : timelineSnapshot.getStartedAt();
        RemoteContext remoteContext = resolveRemoteContext(request, driver);

        return new ReportContext(
                startedAt,
                finishedAt,
                Duration.between(startedAt, finishedAt).toMillis(),
                formatDuration(startedAt, finishedAt),
                outcome,
                safe(testName),
                request == null ? null : safe(request.getExecutionProfileId()),
                request == null ? null : safe(request.getTarget()),
                request == null ? null : safe(request.getDriverType()),
                request == null ? null : safe(request.getDescription()),
                safe(sessionId(driver)),
                safe(currentUrl(driver)),
                safe(pageTitle(driver)),
                safe(mobileContext(driver)),
                safe(mobilePackage(driver)),
                safe(mobileActivity(driver)),
                safe(CapabilitiesSummary.summarize(request == null ? null : request.getCapabilities())),
                stepSnapshot,
                timelineSnapshot,
                buildEventGroups(timelineSnapshot),
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
                safe(rootType(cause)),
                safe(rootMessage(cause)),
                safe(stackTrace(cause)),
                screenshotUri(driver, reportDir),
                remoteContext,
                cause != null
        );
    }

    private static String buildBaseFileName(ReportContext report) {
        String timestamp = FILE_TIME_FORMAT.format(report.finishedAt.atZone(ZoneId.systemDefault()));
        return String.format(
                Locale.ROOT,
                "report-%s-%s-%s",
                timestamp,
                report.outcome.toLowerCase(Locale.ROOT),
                sanitizeFileName(report.testName)
        );
    }

    private static String renderReport(ReportContext report) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>").append(escapeHtml(report.testName)).append("</title>")
                .append("<style>")
                .append(":root{--bg:#f5f7fb;--surface:#ffffff;--surface-alt:#f9fbff;--border:#d8dee9;--text:#1f2937;--muted:#5b6472;--pass:#117a37;--pass-bg:#dafbe1;--fail:#cf222e;--fail-bg:#ffebe9;--action:#0550ae;--action-bg:#ddf4ff;--wait:#7c3aed;--wait-bg:#f3e8ff;--assert:#9a6700;--assert-bg:#fff8c5;--shot:#8250df;--shot-bg:#fbefff;--error:#b42318;--error-bg:#ffe4e6;}")
                .append("*{box-sizing:border-box;}body{margin:0;font-family:Segoe UI,Arial,sans-serif;background:radial-gradient(circle at top,#edf4ff 0,#f5f7fb 35%,#f7f8fb 100%);color:var(--text);}a{color:#0969da;text-decoration:none;}a:hover{text-decoration:underline;}")
                .append(".wrap{max-width:1240px;margin:0 auto;padding:28px 20px 48px;} .hero{background:linear-gradient(135deg,#ffffff 0,#f6faff 100%);border:1px solid var(--border);border-radius:24px;padding:28px;box-shadow:0 18px 40px rgba(24,39,75,.08);} .hero-top{display:flex;justify-content:space-between;gap:16px;align-items:flex-start;flex-wrap:wrap;} .hero h1{margin:10px 0 8px;font-size:30px;line-height:1.15;} .hero-meta{display:flex;flex-wrap:wrap;gap:14px;margin-top:10px;font-size:14px;color:var(--muted);} .hero-actions{display:flex;gap:10px;flex-wrap:wrap;}")
                .append(".status{display:inline-flex;align-items:center;gap:8px;padding:8px 12px;border-radius:999px;font-size:12px;font-weight:700;letter-spacing:.04em;text-transform:uppercase;} .status.passed{background:var(--pass-bg);color:var(--pass);} .status.failed{background:var(--fail-bg);color:var(--fail);} .pill{display:inline-flex;align-items:center;padding:8px 12px;border-radius:999px;background:#eef5ff;color:#244f8f;font-size:12px;font-weight:700;}")
                .append(".metrics{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:12px;margin-top:20px;} .metric,.card,.panel,.timeline-card,.artifact-card{background:var(--surface);border:1px solid var(--border);border-radius:18px;box-shadow:0 10px 24px rgba(16,24,40,.04);} .metric{padding:16px 18px;background:linear-gradient(180deg,#ffffff 0,#fbfdff 100%);} .metric-label{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;} .metric-value{margin-top:8px;font-size:22px;font-weight:700;}")
                .append(".section{margin-top:24px;} .section h2{margin:0 0 12px;font-size:18px;} .grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:16px;} .card,.panel,.artifact-card,.story-card{padding:18px;} .card strong{display:block;font-size:12px;text-transform:uppercase;letter-spacing:.05em;color:var(--muted);margin-bottom:8px;} .value{font-size:15px;line-height:1.45;word-break:break-word;}")
                .append(".keyvals{display:grid;grid-template-columns:minmax(120px,180px) 1fr;gap:10px 16px;align-items:start;} .key{font-weight:700;color:#374151;} .keyvals div{word-break:break-word;} .report-layout{display:grid;grid-template-columns:minmax(0,1.3fr) minmax(300px,.7fr);gap:18px;margin-top:24px;} @media (max-width:980px){.report-layout{grid-template-columns:1fr;}}")
                .append(".badge{display:inline-flex;align-items:center;gap:5px;padding:3px 8px;border-radius:999px;font-size:10px;font-weight:700;letter-spacing:.03em;text-transform:uppercase;margin-right:6px;} .badge-step{background:#eef2ff;color:#3730a3;} .badge-action{background:var(--action-bg);color:var(--action);} .badge-wait{background:var(--wait-bg);color:var(--wait);} .badge-assert{background:var(--assert-bg);color:var(--assert);} .badge-screenshot{background:var(--shot-bg);color:var(--shot);} .badge-error{background:var(--error-bg);color:var(--error);} .badge-pass{background:var(--pass-bg);color:var(--pass);} .badge-fail{background:var(--fail-bg);color:var(--fail);} .timeline{display:flex;flex-direction:column;gap:10px;} .timeline-card{padding:12px 14px;background:linear-gradient(180deg,#fff 0,#fbfcff 100%);} .timeline-card.is-failure{border-color:#ef9a9a;box-shadow:0 10px 24px rgba(207,34,46,.12);} .timeline-card.is-warning{border-color:#d8b4fe;box-shadow:0 10px 24px rgba(124,58,237,.10);} .timeline-head{display:flex;gap:8px;align-items:center;flex-wrap:wrap;} .timeline-time{font-size:11px;color:var(--muted);font-weight:700;} .timeline-message{margin-top:6px;font-size:14px;line-height:1.4;}")
                .append(".attachments{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:10px;margin-top:10px;} .attachment{background:var(--surface-alt);border:1px dashed #c6d1e1;border-radius:12px;padding:10px;} .thumb{display:block;width:100%;max-height:180px;object-fit:cover;border-radius:10px;border:1px solid var(--border);margin-top:8px;background:#fff;} .path{margin-top:6px;font-size:11px;color:var(--muted);word-break:break-word;}")
                .append(".list{display:flex;flex-direction:column;gap:10px;} .list-item{padding:12px 14px;border-radius:14px;background:var(--surface-alt);border:1px solid #e3e8f1;font-size:14px;line-height:1.45;} .stack{margin-top:12px;} details summary{cursor:pointer;font-weight:700;} pre{margin:10px 0 0;padding:16px;background:#0f172a;color:#e2e8f0;border-radius:16px;overflow:auto;white-space:pre-wrap;word-break:break-word;} .empty{padding:22px;border-radius:16px;border:1px dashed #c6d1e1;color:var(--muted);background:#fbfcfe;} .story-card{background:linear-gradient(135deg,#ffffff 0,#f7fbff 100%);border:1px solid var(--border);border-radius:18px;box-shadow:0 12px 24px rgba(16,24,40,.04);} .story-title{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;font-weight:700;margin-bottom:10px;} .story-body{font-size:15px;line-height:1.6;} .artifact-list,.focus-list{display:flex;flex-direction:column;gap:10px;} .artifact-link,.focus-item{display:flex;justify-content:space-between;align-items:center;gap:12px;padding:12px 14px;border-radius:14px;background:var(--surface-alt);border:1px solid #e3e8f1;font-size:14px;} .focus-item{align-items:flex-start;flex-direction:column;} .focus-label{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;font-weight:700;} .focus-value{font-size:14px;line-height:1.5;word-break:break-word;}")
                .append("</style></head><body><div class=\"wrap\">");

        html.append("<section class=\"hero\"><div class=\"hero-top\"><div><span class=\"status ")
                .append(report.failed ? "failed" : "passed").append("\">")
                .append(escapeHtml(report.outcome)).append("</span><h1>")
                .append(escapeHtml(report.testName)).append("</h1><div class=\"hero-meta\"><span>Started ")
                .append(escapeHtml(DISPLAY_TIME_FORMAT.format(report.startedAt.atZone(ZoneId.systemDefault()))))
                .append("</span><span>Finished ")
                .append(escapeHtml(DISPLAY_TIME_FORMAT.format(report.finishedAt.atZone(ZoneId.systemDefault()))))
                .append("</span><span>Duration ")
                .append(escapeHtml(report.duration)).append("</span></div></div><div class=\"hero-actions\">")
                .append(renderPill("Profile", report.profileId))
                .append(renderPill("Target", report.target))
                .append(renderPill("Driver", report.driverType))
                .append("</div></div><div class=\"metrics\">")
                .append(renderMetric("Events", String.valueOf(report.totalEvents)))
                .append(renderMetric("Actions", String.valueOf(report.actionCount)))
                .append(renderMetric("Waits", String.valueOf(report.waitCount)))
                .append(renderMetric("Assertions", report.passedAssertions + " / " + report.failedAssertions))
                .append(renderMetric("Screenshots", String.valueOf(report.screenshotCount)))
                .append(renderMetric("Errors", String.valueOf(report.errorCount)))
                .append("</div></section>");

        html.append("<section class=\"section\"><h2>Execution Story</h2><div class=\"story-card\"><div class=\"story-title\">Quick Read</div><div class=\"story-body\">")
                .append(escapeHtml(executionStory(report)))
                .append("</div></div></section>");

        html.append("<section class=\"section\"><h2>Diagnostic Focus</h2><div class=\"story-card\"><div class=\"story-title\">Where to look first</div><div class=\"focus-list\">")
                .append(renderFocusItem("Primary clue", primaryClue(report)))
                .append(renderFocusItem("Next artifact to open", nextArtifact(report)))
                .append(renderFocusItem("Execution fingerprint", executionFingerprint(report)))
                .append("</div></div></section>");

        html.append("<div class=\"report-layout\"><div>");

        html.append("<section class=\"section\"><h2>Highlights</h2><div class=\"grid\">")
                .append(renderCard("Last Step", report.lastStep))
                .append(renderCard("Last Assertion", report.lastAssertion))
                .append(renderCard("Last Screenshot", report.lastScreenshotPath))
                .append("</div></section>");

        html.append("<section class=\"section\"><h2>Recent Steps</h2><div class=\"panel\">");
        if (report.stepSnapshot.getSteps().isEmpty()) {
            html.append("<div class=\"empty\">No steps were recorded for this test.</div>");
        } else {
            if (report.stepSnapshot.isTruncated()) {
                html.append("<p class=\"muted\">Showing last ").append(report.stepSnapshot.getSteps().size())
                        .append(" of ").append(report.stepSnapshot.getTotalRecorded()).append(" recorded steps.</p>");
            }
            html.append("<div class=\"list\">");
            for (String step : report.stepSnapshot.getSteps()) {
                html.append("<div class=\"list-item\">").append(escapeHtml(step)).append("</div>");
            }
            html.append("</div>");
        }
        html.append("</div></section>");

        html.append("<section class=\"section\"><h2>Timeline</h2><div class=\"timeline\">");
        if (report.eventGroups.isEmpty()) {
            html.append("<div class=\"empty\">No timeline events were recorded for this test.</div>");
        } else {
            for (EventGroup group : report.eventGroups) {
                PepeniumTimeline.Event anchor = group.anchorEvent;
                html.append("<article class=\"timeline-card ")
                        .append(timelineCardClass(anchor))
                        .append("\"><div class=\"timeline-head\">")
                        .append(renderEventTypeBadge(anchor))
                        .append(renderEventStatusBadge(anchor))
                        .append("<span class=\"timeline-time\">").append(escapeHtml(anchor.getTime())).append("</span>")
                        .append("</div><div class=\"timeline-message\">").append(escapeHtml(anchor.getMessage())).append("</div>");
                if (!group.screenshots.isEmpty()) {
                    html.append("<details><summary>Show ")
                            .append(group.screenshots.size())
                            .append(group.screenshots.size() == 1 ? " screenshot" : " screenshots")
                            .append("</summary><div class=\"attachments\">");
                    for (PepeniumTimeline.Event screenshot : group.screenshots) {
                        String screenshotUri = pathToUri(screenshot.getScreenshotPath());
                        html.append("<div class=\"attachment\"><div>")
                                .append(renderEventTypeBadge(screenshot))
                                .append("<span class=\"timeline-time\">").append(escapeHtml(screenshot.getTime())).append("</span></div>")
                                .append("<div class=\"timeline-message\">").append(escapeHtml(screenshot.getMessage())).append("</div>")
                                .append("<a href=\"").append(escapeHtml(screenshotUri)).append("\">Open screenshot</a>")
                                .append("<img class=\"thumb\" src=\"").append(escapeHtml(screenshotUri)).append("\" alt=\"Screenshot preview\">")
                                .append("<div class=\"path\">").append(escapeHtml(screenshot.getScreenshotPath())).append("</div></div>");
                    }
                    html.append("</div></details>");
                }
                html.append("</article>");
            }
        }
        html.append("</div></section>");

        if (report.failed) {
            html.append("<section class=\"section\"><h2>Failure Story</h2><div class=\"story-card\"><div class=\"story-title\">What likely happened</div><div class=\"story-body\">")
                    .append(escapeHtml(failureStory(report)))
                    .append("</div></div></section>");
            html.append("<section class=\"section\"><h2>Failure Summary</h2><div class=\"panel\"><div class=\"keyvals\">")
                    .append(renderKeyValue("Root error type", report.rootType))
                    .append(renderKeyValue("Root error message", report.rootMessage))
                    .append(renderKeyValue("Last step before failure", report.lastStep))
                    .append(renderKeyValue("Last assertion before failure", report.lastAssertion))
                    .append("</div>");
            if (report.stackTrace != null) {
                html.append("<details class=\"stack\"><summary>Stack trace</summary><pre>")
                        .append(escapeHtml(report.stackTrace)).append("</pre></details>");
            }
            html.append("</div></section>");
        }

        html.append("</div><div>");
        html.append("<section class=\"section\"><h2>Execution Context</h2><div class=\"panel\"><div class=\"keyvals\">")
                .append(renderKeyValue("Description", report.description))
                .append(renderKeyValue("Current URL", report.currentUrl))
                .append(renderKeyValue("Page title", report.pageTitle))
                .append(renderKeyValue("Mobile context", report.mobileContext))
                .append(renderKeyValue("Mobile package", report.mobilePackage))
                .append(renderKeyValue("Mobile activity", report.mobileActivity))
                .append(renderKeyValue("Capabilities", report.capabilitiesSummary))
                .append(renderKeyValue("Session", report.sessionId))
                .append("</div></div></section>");

        if (report.remoteContext.enabled) {
            html.append("<section class=\"section\"><h2>Remote Session</h2><div class=\"panel\"><div class=\"keyvals\">")
                    .append(renderKeyValue("Provider", report.remoteContext.provider))
                    .append(renderKeyValue("Server host", report.remoteContext.serverHost))
                    .append(renderKeyValue("Server URL", report.remoteContext.serverUrl))
                    .append(renderKeyValue("Project", report.remoteContext.projectName))
                    .append(renderKeyValue("Build", report.remoteContext.buildName))
                    .append(renderKeyValue("Remote session name", report.remoteContext.remoteSessionName))
                    .append(renderKeyValue("Remote local tunnel", report.remoteContext.localEnabled))
                    .append("</div></div></section>");
        }

        html.append("<section class=\"section\"><h2>Artifacts</h2><div class=\"artifact-card\"><div class=\"artifact-list\">")
                .append(renderArtifactLink("Suite index", "index.html"))
                .append(renderArtifactLink("Suite summary JSON", "summary.json"));
        if (report.screenshotUri != null) {
            html.append(renderArtifactLink("Final screenshot", report.screenshotUri));
        }
        if (report.lastScreenshotPath != null) {
            html.append(renderArtifactLink("Last manual screenshot", pathToUri(report.lastScreenshotPath)));
        }
        if (report.remoteContext.enabled && report.remoteContext.dashboardUrl != null) {
            html.append(renderArtifactLink("Remote dashboard", report.remoteContext.dashboardUrl));
        }
        html.append("</div></div></section>");

        if (report.screenshotUri != null) {
            html.append("<section class=\"section\"><h2>Final Screenshot</h2><div class=\"artifact-card\"><a href=\"")
                    .append(escapeHtml(report.screenshotUri)).append("\">Open screenshot</a><img class=\"thumb\" src=\"")
                    .append(escapeHtml(report.screenshotUri)).append("\" alt=\"Final screenshot\"></div></section>");
        }

        html.append("</div></div></div></body></html>");
        return html.toString();
    }

    private static String renderReportJson(ReportContext report, String htmlFileName) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        appendJsonField(json, "schemaVersion", 1, true);
        appendJsonField(json, "generatedAt", report.finishedAt.toString(), true);
        appendJsonField(json, "htmlReport", htmlFileName, true);
        appendJsonField(json, "outcome", report.outcome, true);
        appendJsonField(json, "testName", report.testName, true);
        appendJsonField(json, "profileId", report.profileId, true);
        appendJsonField(json, "target", report.target, true);
        appendJsonField(json, "driverType", report.driverType, true);
        appendJsonField(json, "description", report.description, true);
        appendJsonField(json, "sessionId", report.sessionId, true);
        json.append("  \"timing\": {\n");
        appendJsonField(json, "startedAt", report.startedAt.toString(), true, 4);
        appendJsonField(json, "finishedAt", report.finishedAt.toString(), true, 4);
        appendJsonField(json, "durationMillis", report.durationMillis, true, 4);
        appendJsonField(json, "durationDisplay", report.duration, false, 4);
        json.append("  },\n");
        json.append("  \"stats\": {\n");
        appendJsonField(json, "events", report.totalEvents, true, 4);
        appendJsonField(json, "actions", report.actionCount, true, 4);
        appendJsonField(json, "waits", report.waitCount, true, 4);
        appendJsonField(json, "passedAssertions", report.passedAssertions, true, 4);
        appendJsonField(json, "failedAssertions", report.failedAssertions, true, 4);
        appendJsonField(json, "screenshots", report.screenshotCount, true, 4);
        appendJsonField(json, "errors", report.errorCount, false, 4);
        json.append("  },\n");
        json.append("  \"highlights\": {\n");
        appendJsonField(json, "lastStep", report.lastStep, true, 4);
        appendJsonField(json, "lastAssertion", report.lastAssertion, true, 4);
        appendJsonField(json, "lastScreenshotPath", report.lastScreenshotPath, false, 4);
        json.append("  },\n");
        json.append("  \"execution\": {\n");
        appendJsonField(json, "currentUrl", report.currentUrl, true, 4);
        appendJsonField(json, "pageTitle", report.pageTitle, true, 4);
        appendJsonField(json, "mobileContext", report.mobileContext, true, 4);
        appendJsonField(json, "mobilePackage", report.mobilePackage, true, 4);
        appendJsonField(json, "mobileActivity", report.mobileActivity, true, 4);
        appendJsonField(json, "capabilitiesSummary", report.capabilitiesSummary, false, 4);
        json.append("  },\n");
        json.append("  \"remote\": {\n");
        appendJsonField(json, "enabled", report.remoteContext.enabled, true, 4);
        appendJsonField(json, "provider", report.remoteContext.provider, true, 4);
        appendJsonField(json, "serverHost", report.remoteContext.serverHost, true, 4);
        appendJsonField(json, "serverUrl", report.remoteContext.serverUrl, true, 4);
        appendJsonField(json, "projectName", report.remoteContext.projectName, true, 4);
        appendJsonField(json, "buildName", report.remoteContext.buildName, true, 4);
        appendJsonField(json, "sessionName", report.remoteContext.remoteSessionName, true, 4);
        appendJsonField(json, "localEnabled", report.remoteContext.localEnabled, false, 4);
        json.append("  },\n");
        json.append("  \"events\": [\n");
        List<PepeniumTimeline.Event> events = report.timelineSnapshot.getEvents();
        for (int i = 0; i < events.size(); i++) {
            PepeniumTimeline.Event event = events.get(i);
            json.append("    {\n");
            appendJsonField(json, "time", event.getTime(), true, 6);
            appendJsonField(json, "type", event.getType().name(), true, 6);
            appendJsonField(json, "status", event.getStatus().name(), true, 6);
            appendJsonField(json, "message", event.getMessage(), true, 6);
            appendJsonField(json, "screenshotPath", event.getScreenshotPath(), false, 6);
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
            json.append("    ").append(quoteJson(steps.get(i)));
            if (i < steps.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");
        json.append("  \"failure\": {\n");
        appendJsonField(json, "failed", report.failed, true, 4);
        appendJsonField(json, "rootType", report.rootType, true, 4);
        appendJsonField(json, "rootMessage", report.rootMessage, true, 4);
        appendJsonField(json, "stackTrace", report.stackTrace, false, 4);
        json.append("  }\n");
        json.append("}\n");
        return json.toString();
    }

    private static Path writeIndex(Path reportDir) throws IOException {
        List<Path> jsonFiles;
        try (Stream<Path> files = Files.list(reportDir)) {
            jsonFiles = files
                    .filter(path -> path.getFileName().toString().startsWith("report-"))
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(PepeniumHtmlReportWriter::lastModifiedSafely).reversed())
                    .collect(Collectors.toList());
        }

        List<ReportSummary> summaries = new ArrayList<>();
        for (Path jsonFile : jsonFiles) {
            ReportSummary summary = loadSummary(jsonFile);
            if (summary != null) {
                summaries.add(summary);
            }
        }

        Files.writeString(reportDir.resolve("summary.json"), renderSuiteSummaryJson(summaries), StandardCharsets.UTF_8);
        Path indexFile = reportDir.resolve("index.html");
        Files.writeString(indexFile, renderIndexHtml(reportDir, summaries), StandardCharsets.UTF_8);
        return indexFile;
    }

    private static ReportSummary loadSummary(Path jsonFile) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = YAML.load(Files.readString(jsonFile, StandardCharsets.UTF_8));
            if (data == null) {
                return null;
            }
            Map<String, Object> timing = mapValue(data.get("timing"));
            Map<String, Object> stats = mapValue(data.get("stats"));
            Map<String, Object> remote = mapValue(data.get("remote"));
            return new ReportSummary(
                    safe(data.get("testName")),
                    safe(data.get("outcome")),
                    safe(data.get("profileId")),
                    safe(data.get("target")),
                    safe(data.get("driverType")),
                    safe(data.get("htmlReport")),
                    safe(data.get("generatedAt")),
                    numberValue(timing.get("durationMillis")),
                    safe(timing.get("durationDisplay")),
                    numberValue(stats.get("screenshots")),
                    safe(remote.get("provider")),
                    safe(remote.get("enabled"))
            );
        } catch (Exception e) {
            log.warn("Failed to read Pepenium report summary from '{}': {}", jsonFile, e.getMessage());
            return null;
        }
    }

    private static String renderSuiteSummaryJson(List<ReportSummary> summaries) {
        long passed = summaries.stream().filter(summary -> "PASSED".equals(summary.outcome)).count();
        long failed = summaries.size() - passed;
        long totalDuration = summaries.stream().mapToLong(summary -> summary.durationMillis).sum();
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        appendJsonField(json, "generatedAt", Instant.now().toString(), true);
        appendJsonField(json, "totalReports", summaries.size(), true);
        appendJsonField(json, "passed", passed, true);
        appendJsonField(json, "failed", failed, true);
        appendJsonField(json, "totalDurationMillis", totalDuration, false);
        json.append("}\n");
        return json.toString();
    }

    private static String renderIndexHtml(Path reportDir, List<ReportSummary> summaries) {
        long passedCount = summaries.stream().filter(summary -> "PASSED".equals(summary.outcome)).count();
        long failedCount = summaries.size() - passedCount;
        long totalDuration = summaries.stream().mapToLong(summary -> summary.durationMillis).sum();
        long remoteRuns = summaries.stream().filter(summary -> "true".equalsIgnoreCase(summary.remoteEnabled)).count();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>Pepenium Reports</title>")
                .append("<style>")
                .append(":root{--bg:#f5f7fb;--surface:#fff;--border:#d8dee9;--text:#1f2937;--muted:#5b6472;--pass:#117a37;--pass-bg:#dafbe1;--fail:#cf222e;--fail-bg:#ffebe9;}")
                .append("*{box-sizing:border-box;}body{margin:0;font-family:Segoe UI,Arial,sans-serif;background:radial-gradient(circle at top,#edf4ff 0,#f5f7fb 35%,#f7f8fb 100%);color:var(--text);}a{color:#0969da;text-decoration:none;}a:hover{text-decoration:underline;}")
                .append(".wrap{max-width:1360px;margin:0 auto;padding:28px 20px 44px;} .hero,.panel,.report-row,.metric{background:var(--surface);border:1px solid var(--border);border-radius:20px;box-shadow:0 14px 32px rgba(16,24,40,.05);}")
                .append(".hero{padding:28px;background:linear-gradient(135deg,#ffffff 0,#f7fbff 100%);} .hero h1{margin:0 0 8px;font-size:32px;} .muted{color:var(--muted);} .metrics{display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:12px;margin-top:20px;} .metric{padding:16px 18px;} .metric-label{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;} .metric-value{margin-top:8px;font-size:24px;font-weight:700;}")
                .append(".filters{display:grid;grid-template-columns:2fr repeat(4,minmax(0,1fr));gap:12px;margin-top:22px;} @media (max-width:980px){.filters{grid-template-columns:1fr;}} input,select{width:100%;padding:12px 14px;border-radius:14px;border:1px solid #c7d2e2;background:#fff;font-size:14px;}")
                .append(".section{margin-top:22px;} .section h2{margin:0 0 12px;font-size:18px;} .breakdowns{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:16px;} .panel{padding:18px;} .tags{display:flex;flex-wrap:wrap;gap:8px;margin-top:10px;} .tag{padding:8px 10px;border-radius:999px;background:#eef5ff;color:#1f4c8f;font-size:12px;font-weight:700;} .ranking{display:flex;flex-direction:column;gap:10px;margin-top:10px;} .ranking-item{display:flex;justify-content:space-between;gap:10px;align-items:center;padding:10px 12px;border-radius:14px;background:#f8fbff;border:1px solid #e3e8f1;font-size:14px;}")
                .append(".reports{display:flex;flex-direction:column;gap:14px;} .report-row{padding:18px;} .row-top,.row-bottom{display:flex;justify-content:space-between;gap:12px;flex-wrap:wrap;align-items:center;} .row-title{font-size:18px;font-weight:700;} .row-meta,.row-tags{display:flex;gap:10px;flex-wrap:wrap;margin-top:10px;}")
                .append(".badge{display:inline-flex;align-items:center;padding:6px 10px;border-radius:999px;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.04em;} .passed{background:var(--pass-bg);color:var(--pass);} .failed{background:var(--fail-bg);color:var(--fail);} .meta{padding:6px 10px;border-radius:999px;background:#eef2f8;color:#344054;font-size:12px;font-weight:700;}")
                .append(".hidden{display:none;} .toolbar{display:flex;justify-content:space-between;gap:12px;align-items:center;flex-wrap:wrap;margin-top:12px;} .small{font-size:13px;}")
                .append("</style><script>")
                .append("function applyFilters(){const q=document.getElementById('search').value.toLowerCase();const status=document.getElementById('status').value;const target=document.getElementById('target').value;const profile=document.getElementById('profile').value;const provider=document.getElementById('provider').value;let visible=0;document.querySelectorAll('.report-row').forEach(row=>{const text=row.dataset.search;const ok=(!q||text.includes(q))&&(!status||row.dataset.status===status)&&(!target||row.dataset.target===target)&&(!profile||row.dataset.profile===profile)&&(!provider||row.dataset.provider===provider);row.classList.toggle('hidden',!ok);if(ok){visible++;}});document.getElementById('visible-count').textContent=visible+' report(s) visible';}")
                .append("</script></head><body><div class=\"wrap\">");

        html.append("<section class=\"hero\"><h1>Pepenium Reports</h1>")
                .append("<p class=\"muted\">Shareable execution reports with per-test HTML, JSON artifacts and suite-level summary under ")
                .append(escapeHtml(reportDir.toAbsolutePath().toString()))
                .append("</p><div class=\"metrics\">")
                .append(renderMetric("Total Reports", String.valueOf(summaries.size())))
                .append(renderMetric("Passed", String.valueOf(passedCount)))
                .append(renderMetric("Failed", String.valueOf(failedCount)))
                .append(renderMetric("Remote Runs", String.valueOf(remoteRuns)))
                .append(renderMetric("Total Duration", formatDurationMillis(totalDuration)))
                .append("</div><div class=\"filters\">")
                .append("<input id=\"search\" type=\"search\" placeholder=\"Search test, profile, target or driver\" oninput=\"applyFilters()\">")
                .append(renderSelect("status", "Status", uniqueValues(summaries, summary -> summary.outcome)))
                .append(renderSelect("target", "Target", uniqueValues(summaries, summary -> summary.target)))
                .append(renderSelect("profile", "Profile", uniqueValues(summaries, summary -> summary.profileId)))
                .append(renderSelect("provider", "Provider", uniqueValues(summaries, summary -> summary.provider)))
                .append("</div><div class=\"toolbar\"><span id=\"visible-count\" class=\"muted small\">")
                .append(summaries.size()).append(" report(s) visible</span><a href=\"summary.json\">Open suite summary JSON</a></div></section>");

        html.append("<section class=\"section\"><h2>Suite Summary</h2><div class=\"breakdowns\">")
                .append(renderBreakdownPanel("By Target", groupCounts(summaries, summary -> summary.target)))
                .append(renderBreakdownPanel("By Profile", groupCounts(summaries, summary -> summary.profileId)))
                .append(renderBreakdownPanel("By Provider", groupCounts(summaries, summary -> summary.provider)))
                .append("</div></section>");

        html.append("<section class=\"section\"><h2>Suite Insights</h2><div class=\"breakdowns\">")
                .append(renderTopListPanel("Slowest Tests", topSlowest(summaries)))
                .append(renderTopListPanel("Most Screenshots", topScreenshots(summaries)))
                .append("</div></section>");

        html.append("<section class=\"section\"><h2>Reports</h2><div class=\"reports\">");
        if (summaries.isEmpty()) {
            html.append("<div class=\"panel\">No reports generated yet.</div>");
        } else {
            for (ReportSummary summary : summaries) {
                String provider = defaultValue(summary.provider);
                String searchText = (
                        defaultValue(summary.testName) + " " +
                        defaultValue(summary.profileId) + " " +
                        defaultValue(summary.target) + " " +
                        defaultValue(summary.driverType) + " " +
                        provider
                ).toLowerCase(Locale.ROOT);
                html.append("<article class=\"report-row\" data-search=\"")
                        .append(escapeHtml(searchText))
                        .append("\" data-status=\"").append(escapeHtml(defaultValue(summary.outcome)))
                        .append("\" data-target=\"").append(escapeHtml(defaultValue(summary.target)))
                        .append("\" data-profile=\"").append(escapeHtml(defaultValue(summary.profileId)))
                        .append("\" data-provider=\"").append(escapeHtml(provider))
                        .append("\"><div class=\"row-top\"><div><div class=\"row-title\"><a href=\"")
                        .append(escapeHtml(summary.htmlReport))
                        .append("\">").append(escapeHtml(summary.testName)).append("</a></div>")
                        .append("<div class=\"row-meta\"><span class=\"badge ")
                        .append("PASSED".equals(summary.outcome) ? "passed" : "failed")
                        .append("\">").append(escapeHtml(summary.outcome)).append("</span>")
                        .append("<span class=\"meta\">").append(escapeHtml(defaultValue(summary.target))).append("</span>")
                        .append("<span class=\"meta\">").append(escapeHtml(defaultValue(summary.profileId))).append("</span>")
                        .append("<span class=\"meta\">").append(escapeHtml(defaultValue(summary.driverType))).append("</span>")
                        .append("</div></div><div class=\"row-bottom\"><span class=\"meta\">")
                        .append(escapeHtml(defaultValue(summary.durationDisplay))).append("</span><span class=\"muted small\">")
                        .append(escapeHtml(defaultValue(summary.generatedAt))).append("</span></div></div>")
                        .append("<div class=\"row-tags\"><span class=\"meta\">Provider: ")
                        .append(escapeHtml(provider)).append("</span><span class=\"meta\">Screenshots: ")
                        .append(summary.screenshotCount).append("</span><a class=\"meta\" href=\"")
                        .append(escapeHtml(summary.jsonReport)).append("\">JSON</a></div></article>");
            }
        }
        html.append("</div></section></div></body></html>");
        return html.toString();
    }

    private static String renderMetric(String title, String value) {
        return "<div class=\"metric\"><div class=\"metric-label\">" + escapeHtml(title) + "</div><div class=\"metric-value\">"
                + escapeHtml(defaultValue(value)) + "</div></div>";
    }

    private static String renderPill(String title, String value) {
        return "<span class=\"pill\">" + escapeHtml(title) + ": " + escapeHtml(defaultValue(value)) + "</span>";
    }

    private static String renderCard(String title, String value) {
        return "<div class=\"card\"><strong>" + escapeHtml(title) + "</strong><div class=\"value\">"
                + escapeHtml(defaultValue(value)) + "</div></div>";
    }

    private static String renderArtifactLink(String label, String href) {
        return "<a class=\"artifact-link\" href=\"" + escapeHtml(defaultValue(href)) + "\"><span>"
                + escapeHtml(label) + "</span><span class=\"muted\">Open</span></a>";
    }

    private static String renderFocusItem(String label, String value) {
        return "<div class=\"focus-item\"><div class=\"focus-label\">" + escapeHtml(label)
                + "</div><div class=\"focus-value\">" + escapeHtml(defaultValue(value)) + "</div></div>";
    }

    private static String renderKeyValue(String title, String value) {
        return "<div class=\"key\">" + escapeHtml(title) + "</div><div>" + escapeHtml(defaultValue(value)) + "</div>";
    }

    private static String executionStory(ReportContext report) {
        StringBuilder story = new StringBuilder();
        story.append("This test ");
        story.append(report.failed ? "failed" : "passed");
        story.append(" after ");
        story.append(report.totalEvents);
        story.append(report.totalEvents == 1 ? " event" : " events");
        story.append(" in ");
        story.append(report.duration);
        story.append(". ");
        if (report.actionCount > 0) {
            story.append("It performed ").append(report.actionCount).append(report.actionCount == 1 ? " action" : " actions").append(". ");
        }
        if (report.waitCount > 0) {
            story.append("It recorded ").append(report.waitCount).append(report.waitCount == 1 ? " wait" : " waits").append(". ");
        }
        if (report.passedAssertions + report.failedAssertions > 0) {
            story.append("Assertions: ").append(report.passedAssertions).append(" passed and ")
                    .append(report.failedAssertions).append(" failed. ");
        }
        if (report.lastStep != null) {
            story.append("Last tracked step: ").append(report.lastStep).append(". ");
        }
        if (report.remoteContext.enabled) {
            story.append("Execution used ").append(defaultValue(report.remoteContext.provider)).append(" as remote provider.");
        }
        return story.toString().trim();
    }

    private static String failureStory(ReportContext report) {
        StringBuilder story = new StringBuilder();
        story.append("The test ended with ");
        story.append(defaultValue(report.rootType));
        if (report.rootMessage != null) {
            story.append(": ").append(report.rootMessage);
        }
        story.append(". ");
        if (report.lastStep != null) {
            story.append("The last recorded step before the failure was \"").append(report.lastStep).append("\". ");
        }
        if (report.lastAssertion != null) {
            story.append("The most recent assertion was \"").append(report.lastAssertion).append("\". ");
        }
        if (report.lastScreenshotPath != null) {
            story.append("There is at least one screenshot artifact captured close to the failing point. ");
        }
        if (report.errorCount > 0) {
            story.append("The timeline also recorded ").append(report.errorCount)
                    .append(report.errorCount == 1 ? " explicit error event." : " explicit error events.");
        }
        return story.toString().trim();
    }

    private static String primaryClue(ReportContext report) {
        if (report.failed) {
            if (report.rootMessage != null) {
                return report.rootMessage;
            }
            if (report.lastAssertion != null) {
                return "Inspect the last assertion: " + report.lastAssertion;
            }
            if (report.lastStep != null) {
                return "Inspect the last recorded step: " + report.lastStep;
            }
            return "Start with the failure summary and the final events in the timeline.";
        }
        if (report.waitCount > 0) {
            return "This execution included waits; check the timeline for slow or repeated wait phases.";
        }
        if (report.screenshotCount > 0) {
            return "Screenshots were captured; the latest one is usually the fastest way to understand the final UI state.";
        }
        return "Use the timeline and highlights to understand the main flow of the execution.";
    }

    private static String nextArtifact(ReportContext report) {
        if (report.failed && report.lastScreenshotPath != null) {
            return "Open the latest screenshot evidence first, then compare it with the failing step and assertion.";
        }
        if (report.failed && report.stackTrace != null) {
            return "Open the stack trace after reviewing the failure story and recent events.";
        }
        if (report.remoteContext.enabled && report.remoteContext.dashboardUrl != null) {
            return "Open the remote dashboard to inspect the provider-side session details.";
        }
        if (report.screenshotUri != null) {
            return "Open the final screenshot to validate the end state captured by the report.";
        }
        return "Open the timeline section and follow the last events in order.";
    }

    private static String executionFingerprint(ReportContext report) {
        StringBuilder value = new StringBuilder();
        value.append(defaultValue(report.target)).append(" / ").append(defaultValue(report.profileId));
        value.append(" / ").append(defaultValue(report.driverType));
        if (report.remoteContext.enabled) {
            value.append(" / ").append(defaultValue(report.remoteContext.provider));
        }
        return value.toString();
    }

    private static String renderEventTypeBadge(PepeniumTimeline.Event event) {
        return "<span class=\"badge " + eventTypeBadgeClass(event.getType()) + "\">" + escapeHtml(event.getType().name()) + "</span>";
    }

    private static String renderEventStatusBadge(PepeniumTimeline.Event event) {
        if (event.getStatus() == PepeniumTimeline.EventStatus.PASSED) {
            return "<span class=\"badge badge-pass\">PASS</span>";
        }
        if (event.getStatus() == PepeniumTimeline.EventStatus.FAILED) {
            return "<span class=\"badge badge-fail\">FAIL</span>";
        }
        return "";
    }

    private static String timelineCardClass(PepeniumTimeline.Event event) {
        if (event.getStatus() == PepeniumTimeline.EventStatus.FAILED || event.getType() == PepeniumTimeline.EventType.ERROR) {
            return "is-failure";
        }
        if (event.getType() == PepeniumTimeline.EventType.WAIT) {
            return "is-warning";
        }
        return "";
    }

    private static String eventTypeBadgeClass(PepeniumTimeline.EventType type) {
        switch (type) {
            case ACTION:
                return "badge-action";
            case WAIT:
                return "badge-wait";
            case ASSERT:
                return "badge-assert";
            case SCREENSHOT:
                return "badge-screenshot";
            case ERROR:
                return "badge-error";
            case STEP:
            default:
                return "badge-step";
        }
    }

    private static List<EventGroup> buildEventGroups(PepeniumTimeline.Snapshot snapshot) {
        List<EventGroup> groups = new ArrayList<>();
        EventGroup current = null;
        for (PepeniumTimeline.Event event : snapshot.getEvents()) {
            if (event.getType() == PepeniumTimeline.EventType.SCREENSHOT && event.getScreenshotPath() != null) {
                if (current == null) {
                    current = new EventGroup(event);
                    groups.add(current);
                } else {
                    current.screenshots.add(event);
                }
                continue;
            }
            current = new EventGroup(event);
            groups.add(current);
        }
        return groups;
    }

    private static int countEvents(
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

    private static String findLastMessage(PepeniumTimeline.Snapshot snapshot, PepeniumTimeline.EventType type) {
        List<PepeniumTimeline.Event> events = snapshot.getEvents();
        for (int i = events.size() - 1; i >= 0; i--) {
            PepeniumTimeline.Event event = events.get(i);
            if (event.getType() == type) {
                return event.getMessage();
            }
        }
        return null;
    }

    private static String findLastScreenshotPath(PepeniumTimeline.Snapshot snapshot) {
        List<PepeniumTimeline.Event> events = snapshot.getEvents();
        for (int i = events.size() - 1; i >= 0; i--) {
            PepeniumTimeline.Event event = events.get(i);
            if (event.getType() == PepeniumTimeline.EventType.SCREENSHOT && event.getScreenshotPath() != null) {
                return event.getScreenshotPath();
            }
        }
        return null;
    }

    private static RemoteContext resolveRemoteContext(DriverRequest request, WebDriver driver) {
        if (request == null) {
            return RemoteContext.disabled();
        }

        URL serverUrl = request.getServerUrl();
        Capabilities capabilities = request.getCapabilities();
        String host = serverUrl == null ? null : serverUrl.getHost();
        String sanitizedUrl = sanitizeServerUrl(serverUrl);
        String driverType = request.getDriverType() == null ? null : request.getDriverType().name();
        String provider = detectProvider(host, driverType);
        String projectName = capabilityString(capabilities, "bstack:options", "projectName");
        String buildName = capabilityString(capabilities, "bstack:options", "buildName");
        String sessionName = capabilityString(capabilities, "bstack:options", "sessionName");
        String localFlag = capabilityString(capabilities, "bstack:options", "local");
        if (localFlag == null) {
            localFlag = capabilityString(capabilities, "browserstack.local", null);
        }

        if (driver instanceof RemoteWebDriver || serverUrl != null) {
            return new RemoteContext(
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
        return RemoteContext.disabled();
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

    private static String capabilityString(Capabilities capabilities, String capabilityKey, String nestedKey) {
        if (capabilities == null) {
            return null;
        }
        Object value = capabilities.getCapability(capabilityKey);
        if (nestedKey == null) {
            return value == null ? null : String.valueOf(value);
        }
        if (value instanceof Map) {
            Object nested = ((Map<?, ?>) value).get(nestedKey);
            return nested == null ? null : String.valueOf(nested);
        }
        return null;
    }

    private static String sanitizeServerUrl(URL url) {
        if (url == null) {
            return null;
        }
        String protocol = url.getProtocol() == null ? "http" : url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();
        String path = url.getPath() == null ? "" : url.getPath();
        StringBuilder value = new StringBuilder(protocol).append("://").append(host);
        if (port > 0) {
            value.append(":").append(port);
        }
        value.append(path);
        return value.toString();
    }

    private static List<String> uniqueValues(List<ReportSummary> summaries, SummarySelector selector) {
        return summaries.stream()
                .map(selector::get)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private static String renderSelect(String id, String label, List<String> values) {
        StringBuilder html = new StringBuilder();
        html.append("<select id=\"").append(id).append("\" aria-label=\"").append(escapeHtml(label))
                .append("\" onchange=\"applyFilters()\"><option value=\"\">")
                .append(escapeHtml(label)).append(": all</option>");
        for (String value : values) {
            html.append("<option value=\"").append(escapeHtml(value)).append("\">")
                    .append(escapeHtml(value)).append("</option>");
        }
        html.append("</select>");
        return html.toString();
    }

    private static String renderBreakdownPanel(String title, Map<String, Long> counts) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"panel\"><h3>").append(escapeHtml(title)).append("</h3>");
        if (counts.isEmpty()) {
            html.append("<p class=\"muted\">No data available yet.</p>");
        } else {
            html.append("<div class=\"tags\">");
            counts.forEach((label, count) -> html.append("<span class=\"tag\">")
                    .append(escapeHtml(defaultValue(label)))
                    .append(" · ")
                    .append(count)
                    .append("</span>"));
            html.append("</div>");
        }
        html.append("</div>");
        return html.toString();
    }

    private static String renderTopListPanel(String title, List<ReportSummary> summaries) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"panel\"><h3>").append(escapeHtml(title)).append("</h3>");
        if (summaries.isEmpty()) {
            html.append("<p class=\"muted\">No data available yet.</p>");
        } else {
            html.append("<div class=\"ranking\">");
            for (ReportSummary summary : summaries) {
                html.append("<div class=\"ranking-item\"><a href=\"")
                        .append(escapeHtml(summary.htmlReport))
                        .append("\">")
                        .append(escapeHtml(defaultValue(summary.testName)))
                        .append("</a><span class=\"meta\">")
                        .append(escapeHtml(topMetric(summary, title)))
                        .append("</span></div>");
            }
            html.append("</div>");
        }
        html.append("</div>");
        return html.toString();
    }

    private static String topMetric(ReportSummary summary, String title) {
        if ("Most Screenshots".equals(title)) {
            return summary.screenshotCount + " screenshots";
        }
        return defaultValue(summary.durationDisplay);
    }

    private static List<ReportSummary> topSlowest(List<ReportSummary> summaries) {
        return summaries.stream()
                .sorted(Comparator.comparingLong((ReportSummary summary) -> summary.durationMillis).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private static List<ReportSummary> topScreenshots(List<ReportSummary> summaries) {
        return summaries.stream()
                .sorted(Comparator.comparingLong((ReportSummary summary) -> summary.screenshotCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private static Map<String, Long> groupCounts(List<ReportSummary> summaries, SummarySelector selector) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ReportSummary summary : summaries) {
            String value = defaultValue(selector.get(summary));
            counts.put(value, counts.getOrDefault(value, 0L) + 1);
        }
        return counts;
    }

    private static void appendJsonField(StringBuilder json, String key, Object value, boolean withComma) {
        appendJsonField(json, key, value, withComma, 2);
    }

    private static void appendJsonField(StringBuilder json, String key, Object value, boolean withComma, int indent) {
        json.append(" ".repeat(indent)).append(quoteJson(key)).append(": ");
        if (value == null) {
            json.append("null");
        } else if (value instanceof Number || value instanceof Boolean) {
            json.append(value);
        } else {
            json.append(quoteJson(String.valueOf(value)));
        }
        if (withComma) {
            json.append(",");
        }
        json.append("\n");
    }

    private static String quoteJson(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t") + "\"";
    }

    private static Map<String, Object> mapValue(Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cast = (Map<String, Object>) value;
            return cast;
        }
        return new LinkedHashMap<>();
    }

    private static long numberValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    private static String formatDuration(Instant startedAt, Instant finishedAt) {
        return formatDurationMillis(Duration.between(startedAt, finishedAt).toMillis());
    }

    private static String formatDurationMillis(long millis) {
        Duration duration = Duration.ofMillis(Math.max(millis, 0L));
        long totalSeconds = duration.getSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long remainingMillis = duration.toMillisPart();
        if (minutes > 0) {
            return String.format(Locale.ROOT, "%dm %02ds %03dms", minutes, seconds, remainingMillis);
        }
        return String.format(Locale.ROOT, "%ds %03dms", seconds, remainingMillis);
    }

    private static Instant lastModifiedSafely(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException e) {
            return Instant.EPOCH;
        }
    }

    private static String screenshotUri(WebDriver driver, Path reportDir) {
        if (!(driver instanceof TakesScreenshot)) {
            return null;
        }
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path screenshotDir = reportDir.resolve("screenshots");
            Files.createDirectories(screenshotDir);
            Path screenshotPath = screenshotDir.resolve("report_" + Instant.now().toEpochMilli() + ".png");
            Files.write(screenshotPath, screenshot);
            return screenshotPath.toUri().toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String pathToUri(String path) {
        return path == null ? null : Path.of(path).toUri().toString();
    }

    private static Path resolveReportDir() {
        String override = System.getProperty("pepenium.report.dir");
        if (override == null || override.isBlank()) {
            override = System.getenv("PEPENIUM_REPORT_DIR");
        }
        return override == null || override.isBlank()
                ? reportDirHint()
                : Paths.get(override);
    }

    private static Path reportDirHint() {
        return Paths.get("target", "pepenium-reports");
    }

    private static String sessionId(WebDriver driver) {
        if (driver instanceof RemoteWebDriver) {
            try {
                return String.valueOf(((RemoteWebDriver) driver).getSessionId());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private static String currentUrl(WebDriver driver) {
        try {
            return driver == null ? null : driver.getCurrentUrl();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String pageTitle(WebDriver driver) {
        try {
            return driver == null ? null : driver.getTitle();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String mobileContext(WebDriver driver) {
        if (!(driver instanceof AppiumDriver)) {
            return null;
        }
        try {
            Object context = driver.getClass().getMethod("getContext").invoke(driver);
            return context == null ? null : String.valueOf(context);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String mobilePackage(WebDriver driver) {
        if (!(driver instanceof AndroidDriver)) {
            return null;
        }
        try {
            return ((AndroidDriver) driver).getCurrentPackage();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String mobileActivity(WebDriver driver) {
        if (driver instanceof AndroidDriver) {
            try {
                return ((AndroidDriver) driver).currentActivity();
            } catch (Exception ignored) {
                return null;
            }
        }
        if (driver instanceof IOSDriver) {
            return "n/a";
        }
        return null;
    }

    private static String rootType(Throwable cause) {
        Throwable root = rootCause(cause);
        return root == null ? null : root.getClass().getSimpleName();
    }

    private static String rootMessage(Throwable cause) {
        Throwable root = rootCause(cause);
        if (root == null || root.getMessage() == null || root.getMessage().isBlank()) {
            return null;
        }
        return root.getMessage().replaceAll("\\s+", " ").trim();
    }

    private static String stackTrace(Throwable cause) {
        if (cause == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private static String safe(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String defaultValue(String value) {
        return value == null || value.isBlank() ? "n/a" : value;
    }

    private static String sanitizeFileName(String input) {
        String sanitized = input == null ? "unnamed-test" : input.replaceAll("[^a-zA-Z0-9._-]+", "_");
        return sanitized.isBlank() ? "unnamed-test" : sanitized;
    }

    private static String escapeHtml(String raw) {
        if (raw == null) {
            return "n/a";
        }
        return raw
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @FunctionalInterface
    private interface SummarySelector {
        String get(ReportSummary summary);
    }

    private static final class EventGroup {
        private final PepeniumTimeline.Event anchorEvent;
        private final List<PepeniumTimeline.Event> screenshots = new ArrayList<>();

        private EventGroup(PepeniumTimeline.Event anchorEvent) {
            this.anchorEvent = anchorEvent;
            if (anchorEvent.getType() == PepeniumTimeline.EventType.SCREENSHOT && anchorEvent.getScreenshotPath() != null) {
                this.screenshots.add(anchorEvent);
            }
        }
    }

    private static final class RemoteContext {
        private final boolean enabled;
        private final String provider;
        private final String serverHost;
        private final String serverUrl;
        private final String projectName;
        private final String buildName;
        private final String remoteSessionName;
        private final String localEnabled;
        private final String dashboardUrl;

        private RemoteContext(
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

        private static RemoteContext disabled() {
            return new RemoteContext(false, null, null, null, null, null, null, null, null);
        }
    }

    private static final class ReportSummary {
        private final String testName;
        private final String outcome;
        private final String profileId;
        private final String target;
        private final String driverType;
        private final String htmlReport;
        private final String jsonReport;
        private final String generatedAt;
        private final long durationMillis;
        private final String durationDisplay;
        private final long screenshotCount;
        private final String provider;
        private final String remoteEnabled;

        private ReportSummary(
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

    private static final class ReportContext {
        private final Instant startedAt;
        private final Instant finishedAt;
        private final long durationMillis;
        private final String duration;
        private final String outcome;
        private final String testName;
        private final String profileId;
        private final String target;
        private final String driverType;
        private final String description;
        private final String sessionId;
        private final String currentUrl;
        private final String pageTitle;
        private final String mobileContext;
        private final String mobilePackage;
        private final String mobileActivity;
        private final String capabilitiesSummary;
        private final StepTracker.Snapshot stepSnapshot;
        private final PepeniumTimeline.Snapshot timelineSnapshot;
        private final List<EventGroup> eventGroups;
        private final int totalEvents;
        private final int actionCount;
        private final int waitCount;
        private final int passedAssertions;
        private final int failedAssertions;
        private final int screenshotCount;
        private final int errorCount;
        private final String lastStep;
        private final String lastAssertion;
        private final String lastScreenshotPath;
        private final String rootType;
        private final String rootMessage;
        private final String stackTrace;
        private final String screenshotUri;
        private final RemoteContext remoteContext;
        private final boolean failed;

        private ReportContext(
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
