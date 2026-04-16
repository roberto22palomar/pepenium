package io.github.roberto22palomar.pepenium.core.observability;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class PepeniumReportHtmlRenderer {

    private PepeniumReportHtmlRenderer() {
    }

    static String render(PepeniumHtmlReportWriter.ReportContext report) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>").append(PepeniumReportSupport.escapeHtml(report.testName)).append("</title>")
                .append("<style>")
                .append(":root{--bg:#eef3f8;--surface:#ffffff;--surface-alt:#f7fafc;--border:#d7dee8;--border-strong:#bcc8d8;--text:#17212f;--muted:#617085;--hero-ink:#0f2942;--accent:#0b6bcb;--accent-soft:#e7f2ff;--pass:#117a37;--pass-bg:#dafbe1;--fail:#cf222e;--fail-bg:#ffebe9;--action:#0550ae;--action-bg:#ddf4ff;--wait:#7c3aed;--wait-bg:#f3e8ff;--assert:#9a6700;--assert-bg:#fff8c5;--shot:#8250df;--shot-bg:#fbefff;--error:#b42318;--error-bg:#ffe4e6;}")
                .append("*{box-sizing:border-box;}body{margin:0;font-family:Inter,Segoe UI,Arial,sans-serif;background:radial-gradient(circle at top left,#ddeeff 0,#eef3f8 30%,#f6f8fb 100%);color:var(--text);}a{color:#0969da;text-decoration:none;}a:hover{text-decoration:underline;}")
                .append(".wrap{max-width:1240px;margin:0 auto;padding:28px 20px 48px;} .hero{position:relative;background:linear-gradient(135deg,#fcfeff 0,#f4f9ff 55%,#eef5fb 100%);border:1px solid var(--border);border-radius:28px;padding:28px;box-shadow:0 18px 40px rgba(24,39,75,.08);overflow:hidden;} .hero:before{content:'';position:absolute;inset:-80px auto auto -40px;width:220px;height:220px;border-radius:999px;background:radial-gradient(circle,#d7e9ff 0,rgba(215,233,255,0) 70%);} .hero:after{content:'';position:absolute;inset:auto -60px -80px auto;width:240px;height:240px;border-radius:999px;background:radial-gradient(circle,#e6f4ea 0,rgba(230,244,234,0) 72%);} .hero-top,.metrics{position:relative;z-index:1;} .hero-top{display:flex;justify-content:space-between;gap:16px;align-items:flex-start;flex-wrap:wrap;} .hero h1{margin:10px 0 8px;font-size:31px;line-height:1.1;letter-spacing:-.03em;color:var(--hero-ink);} .hero-meta{display:flex;flex-wrap:wrap;gap:14px;margin-top:10px;font-size:14px;color:var(--muted);} .hero-actions{display:flex;gap:10px;flex-wrap:wrap;} .brand{display:flex;justify-content:center;align-items:center;width:100%;margin-bottom:10px;} .brand-title{font-size:28px;font-weight:900;letter-spacing:-.08em;color:var(--hero-ink);text-transform:lowercase;} @media (max-width:640px){.hero{padding:22px 18px;}.brand-title{font-size:24px;}.hero h1{font-size:27px;}.hero-meta{gap:10px;font-size:13px;}}")
                .append(".status{display:inline-flex;align-items:center;gap:8px;padding:8px 12px;border-radius:999px;font-size:12px;font-weight:700;letter-spacing:.04em;text-transform:uppercase;} .status.passed{background:var(--pass-bg);color:var(--pass);} .status.failed{background:var(--fail-bg);color:var(--fail);} .pill{display:inline-flex;align-items:center;padding:8px 12px;border-radius:999px;background:#eef5ff;color:#244f8f;font-size:12px;font-weight:700;}")
                .append(".metrics{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:12px;margin-top:20px;} .metric,.card,.panel,.timeline-card,.artifact-card{background:var(--surface);border:1px solid var(--border);border-radius:18px;box-shadow:0 10px 24px rgba(16,24,40,.04);} .metric{padding:16px 18px;background:linear-gradient(140deg,#ffffff 0,#fbfdff 55%,#f3f8ff 100%);} .metric:nth-child(1){background:linear-gradient(140deg,#ffffff 0,#f9fbff 55%,#eef6ff 100%);} .metric:nth-child(2){background:linear-gradient(140deg,#ffffff 0,#f9fbff 55%,#e8f4ff 100%);} .metric:nth-child(3){background:linear-gradient(140deg,#ffffff 0,#fcf8ff 55%,#f3e8ff 100%);} .metric:nth-child(4){background:linear-gradient(140deg,#ffffff 0,#fbfcff 55%,#eef2ff 100%);} .metric:nth-child(5){background:linear-gradient(140deg,#ffffff 0,#fffdf7 55%,#fff4c7 100%);} .metric:nth-child(6){background:linear-gradient(140deg,#ffffff 0,#fff9ff 55%,#fbefff 100%);} .metric:nth-child(7){background:linear-gradient(140deg,#ffffff 0,#fff7f7 55%,#ffe4e6 100%);} .metric-label{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;} .metric-value{margin-top:8px;font-size:22px;font-weight:700;} .metric-split{display:flex;align-items:baseline;gap:8px;flex-wrap:wrap;margin-top:8px;} .metric-pass{font-size:22px;font-weight:800;color:var(--pass);} .metric-fail{font-size:22px;font-weight:800;color:var(--fail);} .metric-divider{font-size:18px;color:var(--muted);font-weight:700;}")
                .append(".section{margin-top:24px;} .section h2{margin:0 0 12px;font-size:18px;padding-left:12px;border-left:4px solid #8cb8ff;letter-spacing:-.01em;} .grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:16px;} .card,.panel,.artifact-card,.story-card{padding:18px;} .panel,.artifact-card,.story-card{border-color:var(--border-strong);} .card strong{display:block;font-size:12px;text-transform:uppercase;letter-spacing:.05em;color:var(--muted);margin-bottom:8px;} .value{font-size:15px;line-height:1.45;word-break:break-word;}")
                .append(".keyvals{display:grid;grid-template-columns:minmax(120px,180px) 1fr;gap:10px 16px;align-items:start;} .key{font-weight:700;color:#374151;} .keyvals div{word-break:break-word;} .report-layout{display:grid;grid-template-columns:minmax(0,1.3fr) minmax(300px,.7fr);gap:18px;margin-top:24px;} .sidebar{display:flex;flex-direction:column;gap:0;align-self:start;position:sticky;top:18px;} @media (max-width:980px){.report-layout{grid-template-columns:1fr;}.sidebar{position:static;}}")
                .append(".badge{display:inline-flex;align-items:center;gap:5px;padding:3px 8px;border-radius:999px;font-size:10px;font-weight:700;letter-spacing:.03em;text-transform:uppercase;margin-right:6px;} .badge-step{background:#eef2ff;color:#3730a3;} .badge-action{background:var(--action-bg);color:var(--action);} .badge-wait{background:var(--wait-bg);color:var(--wait);} .badge-assert{background:var(--assert-bg);color:var(--assert);} .badge-screenshot{background:var(--shot-bg);color:var(--shot);} .badge-error{background:var(--error-bg);color:var(--error);} .badge-pass{background:var(--pass-bg);color:var(--pass);} .badge-fail{background:var(--fail-bg);color:var(--fail);} .timeline{display:flex;flex-direction:column;gap:10px;} .timeline-card{padding:12px 14px;background:linear-gradient(180deg,#fff 0,#fbfcff 100%);} .timeline-card.is-failure{border-color:#ef9a9a;box-shadow:0 10px 24px rgba(207,34,46,.12);} .timeline-card.is-warning{border-color:#d8b4fe;box-shadow:0 10px 24px rgba(124,58,237,.10);} .timeline-head{display:flex;gap:8px;align-items:center;flex-wrap:wrap;} .timeline-time{font-size:11px;color:var(--muted);font-weight:700;} .timeline-meta{display:flex;gap:8px;flex-wrap:wrap;margin-top:6px;} .timeline-pill{display:inline-flex;align-items:center;padding:3px 8px;border-radius:999px;background:#eef2f8;color:#475467;font-size:10px;font-weight:700;} .timeline-message{margin-top:6px;font-size:14px;line-height:1.4;}")
                .append(".attachments{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:10px;margin-top:10px;} .attachment{background:var(--surface-alt);border:1px dashed #c6d1e1;border-radius:12px;padding:10px;} .thumb{display:block;width:100%;max-height:180px;object-fit:cover;border-radius:10px;border:1px solid var(--border);margin-top:8px;background:#fff;} .path{margin-top:6px;font-size:11px;color:var(--muted);word-break:break-word;}")
                .append(".list{display:flex;flex-direction:column;gap:10px;} .list-item{padding:12px 14px;border-radius:14px;background:var(--surface-alt);border:1px solid #e3e8f1;font-size:14px;line-height:1.45;} .stack{margin-top:12px;} details summary{cursor:pointer;font-weight:700;} pre{margin:10px 0 0;padding:16px;background:#0f172a;color:#e2e8f0;border-radius:16px;overflow:auto;white-space:pre-wrap;word-break:break-word;} .empty{padding:22px;border-radius:16px;border:1px dashed #c6d1e1;color:var(--muted);background:#fbfcfe;} .story-card{background:linear-gradient(135deg,#ffffff 0,#f7fbff 100%);border:1px solid var(--border);border-radius:18px;box-shadow:0 12px 24px rgba(16,24,40,.04);} .story-title{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;font-weight:700;margin-bottom:10px;} .story-body{font-size:15px;line-height:1.6;} .artifact-list,.focus-list{display:flex;flex-direction:column;gap:10px;} .artifact-link,.focus-item{display:flex;justify-content:space-between;align-items:center;gap:12px;padding:12px 14px;border-radius:14px;background:var(--surface-alt);border:1px solid #e3e8f1;font-size:14px;} .focus-item{align-items:flex-start;flex-direction:column;} .focus-label{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;font-weight:700;} .focus-value{font-size:14px;line-height:1.5;word-break:break-word;} .story-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:14px;} .story-metric{padding:14px 16px;border-radius:16px;border:1px solid #dce5f2;background:linear-gradient(180deg,#fff 0,#fbfdff 100%);} .story-metric:nth-child(1){background:linear-gradient(145deg,#ffffff 0,#f8fbff 60%,#eaf3ff 100%);} .story-metric:nth-child(2){background:linear-gradient(145deg,#ffffff 0,#fcf8ff 60%,#f3e8ff 100%);} .story-metric:nth-child(3){background:linear-gradient(145deg,#ffffff 0,#fff9f8 60%,#ffe9d6 100%);} .story-metric:nth-child(4){background:linear-gradient(145deg,#ffffff 0,#f8fff8 60%,#e6f7e8 100%);} .story-metric-label{font-size:12px;text-transform:uppercase;letter-spacing:.05em;color:var(--muted);font-weight:700;} .story-metric-value{margin-top:8px;font-size:20px;font-weight:700;} .block-list{display:flex;flex-direction:column;gap:12px;} .block-card{padding:16px;border-radius:18px;border:1px solid #dce5f2;background:linear-gradient(180deg,#fff 0,#fbfdff 100%);border-left:6px solid #9ec5fe;} .block-head{display:flex;justify-content:space-between;gap:12px;align-items:flex-start;flex-wrap:wrap;} .block-title{font-size:16px;font-weight:700;} .block-subtitle{margin-top:4px;color:var(--muted);font-size:13px;} .block-kpis{display:flex;gap:8px;flex-wrap:wrap;} .chip{display:inline-flex;align-items:center;padding:4px 8px;border-radius:999px;background:#eef2f8;color:#475467;font-size:11px;font-weight:700;} .inline-list{margin-top:12px;display:flex;flex-wrap:wrap;gap:8px;} .inline-item{padding:6px 10px;border-radius:999px;background:#f5f8ff;border:1px solid #d9e4f5;font-size:12px;} .compact-details{margin-top:12px;} .compact-details summary{font-size:13px;} .timeline-details{margin-top:14px;} .timeline-details summary{padding:12px 14px;border:1px solid var(--border);border-radius:14px;background:#fff;} .remote-note{margin-top:12px;padding:12px 14px;border-radius:14px;background:#eef6ff;border:1px solid #cfe0ff;font-size:13px;line-height:1.5;color:#244f8f;} .timeline-preview{display:flex;flex-direction:column;gap:10px;} .more-link{margin-top:12px;padding:12px 14px;border-radius:14px;background:#f6f9ff;border:1px dashed #b9cdee;color:#244f8f;font-size:13px;font-weight:700;} .more-link summary{font-size:13px;} .block-card.is-failure{border-left-color:#ef4444;background:linear-gradient(180deg,#fff 0,#fff8f8 100%);} .block-card.is-wait-heavy{border-left-color:#8b5cf6;background:linear-gradient(180deg,#fff 0,#fcf9ff 100%);}")
                .append("</style></head><body><div class=\"wrap\">");

        html.append("<section class=\"hero\"><div class=\"brand\"><div class=\"brand-title\">pepenium</div></div><div class=\"hero-top\"><div><span class=\"status ")
                .append(report.failed ? "failed" : "passed").append("\">")
                .append(PepeniumReportSupport.escapeHtml(report.outcome)).append("</span><h1>")
                .append(PepeniumReportSupport.escapeHtml(report.testName)).append("</h1><div class=\"hero-meta\"><span>Started ")
                .append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.DISPLAY_TIME_FORMAT.format(report.startedAt.atZone(ZoneId.systemDefault()))))
                .append("</span><span>Finished ")
                .append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.DISPLAY_TIME_FORMAT.format(report.finishedAt.atZone(ZoneId.systemDefault()))))
                .append("</span><span>Duration ")
                .append(PepeniumReportSupport.escapeHtml(report.duration)).append("</span></div></div><div class=\"hero-actions\">")
                .append(renderPill("Profile", report.profileId))
                .append(renderPill("Target", report.target))
                .append(renderPill("Driver", report.driverType))
                .append("</div></div><div class=\"metrics\">")
                .append(renderMetric("Events", String.valueOf(report.totalEvents)))
                .append(renderMetric("Actions", String.valueOf(report.actionCount)))
                .append(renderMetric("Waits", String.valueOf(report.waitCount)))
                .append(renderMetric("Key Signals", String.valueOf(report.keyEventCount)))
                .append(renderAssertionMetric(report))
                .append(renderMetric("Screenshots", String.valueOf(report.screenshotCount)))
                .append(renderMetric("Errors", String.valueOf(report.errorCount)))
                .append("</div></section>");

        html.append("<section class=\"section\"><h2>Execution Story</h2><div class=\"story-card\"><div class=\"story-title\">Quick Read</div><div class=\"story-body\">")
                .append(PepeniumReportSupport.escapeHtml(executionStory(report)))
                .append("</div></div></section>");

        html.append("<section class=\"section\"><h2>Diagnostic Focus</h2><div class=\"story-card\"><div class=\"story-title\">Where to look first</div><div class=\"focus-list\">")
                .append(renderFocusItem("Primary clue", primaryClue(report)))
                .append(renderFocusItem("Next artifact to open", nextArtifact(report)))
                .append(renderFocusItem("Execution fingerprint", executionFingerprint(report)))
                .append("</div></div></section>");

        html.append("<section class=\"section\"><h2>Visual Summary</h2><div class=\"story-grid\">")
                .append(renderStoryMetric("Flow blocks", String.valueOf(report.flowBlocks.size())))
                .append(renderStoryMetric("Key events", String.valueOf(report.keyEventCount)))
                .append(renderStoryMetric("Remote provider", remoteProviderLabel(report)))
                .append(renderStoryMetric("Final location", finalLocation(report)))
                .append("</div>");
        if (report.remoteContext.enabled) {
            html.append("<div class=\"remote-note\">This HTML report is generated the same way for local, BrowserStack, and AWS runs. On remote providers you typically inspect it from the downloaded report artifacts, while provider-specific video, logs, and device details stay in the remote dashboard.</div>");
        }
        html.append("</section>");

        html.append("<div class=\"report-layout\"><div>");

        html.append("<section class=\"section\"><h2>Execution Blocks</h2><div class=\"panel\">");
        if (report.flowBlocks.isEmpty()) {
            html.append("<div class=\"empty\">No higher-level execution blocks could be derived from the timeline. Use the raw timeline for full detail.</div>");
        } else {
            html.append("<div class=\"block-list\">");
            for (PepeniumHtmlReportWriter.FlowBlock block : report.flowBlocks) {
                html.append(renderFlowBlock(block));
            }
            html.append("</div>");
        }
        html.append("</div></section>");

        html.append("<section class=\"section\"><h2>Highlights</h2><div class=\"grid\">")
                .append(renderCard("Last Step", report.lastStep))
                .append(renderCard("Last Assertion", report.lastAssertion))
                .append(renderCard("Last Screenshot", report.lastScreenshotPath))
                .append("</div></section>");

        html.append("<section class=\"section\"><h2>Key Timeline</h2><div class=\"panel\">");
        java.util.List<PepeniumTimeline.Event> keyTimelineEvents = keyTimelineEvents(report);
        if (keyTimelineEvents.isEmpty()) {
            html.append("<div class=\"empty\">No timeline events were recorded for this test.</div>");
        } else {
            html.append("<div class=\"timeline-preview\">");
            int previewCount = Math.min(5, keyTimelineEvents.size());
            for (int i = 0; i < previewCount; i++) {
                html.append(renderTimelineCard(keyTimelineEvents.get(i), report.startedAt, null, report.reportDir));
            }
            html.append("</div>");
            if (keyTimelineEvents.size() > previewCount) {
                html.append("<details class=\"more-link\"><summary>View more key events (")
                        .append(keyTimelineEvents.size() - previewCount).append(" more)</summary><div class=\"timeline\" style=\"margin-top:12px;\">");
                for (int i = previewCount; i < keyTimelineEvents.size(); i++) {
                    html.append(renderTimelineCard(keyTimelineEvents.get(i), report.startedAt, null, report.reportDir));
                }
                html.append("</div></details>");
            }
        }
        html.append("</div></section>");

        html.append("<section class=\"section\"><h2>Wait Hotspots</h2><div class=\"panel\">");
        if (report.waitHotspots.isEmpty()) {
            html.append("<div class=\"empty\">No repeated wait hotspots were detected in this execution.</div>");
        } else {
            html.append("<div class=\"list\">");
            for (PepeniumHtmlReportWriter.WaitHotspot hotspot : report.waitHotspots) {
                html.append(renderWaitHotspot(hotspot));
            }
            html.append("</div>");
        }
        html.append("</div></section>");

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
                html.append("<div class=\"list-item\">").append(PepeniumReportSupport.escapeHtml(step)).append("</div>");
            }
            html.append("</div>");
        }
        html.append("</div></section>");

        html.append("<section class=\"section\"><h2>Timeline</h2><div class=\"panel\"><details class=\"timeline-details\"><summary>Open raw timeline (")
                .append(report.eventGroups.size()).append(report.eventGroups.size() == 1 ? " grouped event" : " grouped events")
                .append(")</summary><div class=\"timeline\" style=\"margin-top:14px;\">");
        if (report.eventGroups.isEmpty()) {
            html.append("<div class=\"empty\">No timeline events were recorded for this test.</div>");
        } else {
            for (PepeniumHtmlReportWriter.EventGroup group : report.eventGroups) {
                html.append(renderTimelineCard(group.anchorEvent, report.startedAt, group, report.reportDir));
            }
        }
        html.append("</div></details></div></section>");

        if (report.failed) {
            html.append("<section class=\"section\"><h2>Failure Story</h2><div class=\"story-card\"><div class=\"story-title\">What likely happened</div><div class=\"story-body\">")
                    .append(PepeniumReportSupport.escapeHtml(failureStory(report)))
                    .append("</div></div></section>");
            html.append("<section class=\"section\"><h2>Failure Summary</h2><div class=\"panel\"><div class=\"keyvals\">")
                    .append(renderKeyValue("Root error type", report.rootType))
                    .append(renderKeyValue("Root error message", report.rootMessage))
                    .append(renderKeyValue("Last step before failure", report.lastStep))
                    .append(renderKeyValue("Last assertion before failure", report.lastAssertion))
                    .append("</div>");
            if (report.stackTrace != null) {
                html.append("<details class=\"stack\"><summary>Stack trace</summary><pre>")
                        .append(PepeniumReportSupport.escapeHtml(report.stackTrace)).append("</pre></details>");
            }
            html.append("</div></section>");
        }

        html.append("</div><div class=\"sidebar\">");
        html.append("<section class=\"section\"><h2>Execution Context</h2><div class=\"panel\"><div class=\"keyvals\">")
                .append(renderKeyValue("Description", report.description))
                .append(renderKeyValue("Platform", report.deviceContext.platformName))
                .append(renderKeyValue("Platform version", report.deviceContext.platformVersion))
                .append(renderKeyValue("Device", report.deviceContext.deviceName))
                .append(renderKeyValue("Browser", report.deviceContext.browserName))
                .append(renderKeyValue("Browser version", report.deviceContext.browserVersion))
                .append(renderKeyValue("Automation", report.deviceContext.automationName))
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

        List<String> manualScreenshotHrefs = manualScreenshotHrefs(report);

        html.append("<section class=\"section\"><h2>Artifacts</h2><div class=\"artifact-card\"><div class=\"artifact-list\">")
                .append(renderArtifactLink("Suite index", "index.html"))
                .append(renderArtifactLink("Suite summary JSON", "summary.json"));
        if (report.screenshotUri != null) {
            html.append(renderArtifactLink("Final screenshot", report.screenshotUri));
        }
        if (report.lastScreenshotPath != null) {
            html.append(renderArtifactLink("Last manual screenshot", PepeniumReportSupport.pathToHref(report.lastScreenshotPath, report.reportDir)));
        }
        if (report.remoteContext.enabled && report.remoteContext.dashboardUrl != null) {
            html.append(renderArtifactLink("Remote dashboard", report.remoteContext.dashboardUrl));
        }
        html.append("</div></div></section>");

        if (!manualScreenshotHrefs.isEmpty()) {
            html.append("<section class=\"section\"><h2>Screenshots</h2><div class=\"artifact-card\"><div class=\"attachments\">");
            int screenshotNumber = 1;
            for (String screenshotHref : manualScreenshotHrefs) {
                html.append(renderScreenshotAttachment("Screenshot " + screenshotNumber, screenshotHref));
                screenshotNumber++;
            }
            html.append("</div></div></section>");
        }

        if (report.screenshotUri != null) {
            html.append("<section class=\"section\"><h2>Final Screenshot</h2><div class=\"artifact-card\"><a href=\"")
                    .append(PepeniumReportSupport.escapeHtml(report.screenshotUri)).append("\">Open screenshot</a><img class=\"thumb\" src=\"")
                    .append(PepeniumReportSupport.escapeHtml(report.screenshotUri)).append("\" alt=\"Final screenshot\"></div></section>");
        }

        html.append("</div></div></div></body></html>");
        return html.toString();
    }

    private static String renderMetric(String title, String value) {
        return "<div class=\"metric\"><div class=\"metric-label\">" + PepeniumReportSupport.escapeHtml(title)
                + "</div><div class=\"metric-value\">" + PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(value))
                + "</div></div>";
    }

    private static String renderAssertionMetric(PepeniumHtmlReportWriter.ReportContext report) {
        return "<div class=\"metric\"><div class=\"metric-label\">Assertions</div><div class=\"metric-split\">"
                + "<span class=\"metric-pass\">" + report.passedAssertions + "</span>"
                + "<span class=\"metric-divider\">/</span>"
                + "<span class=\"metric-fail\">" + report.failedAssertions + "</span>"
                + "</div></div>";
    }

    private static String renderPill(String title, String value) {
        return "<span class=\"pill\">" + PepeniumReportSupport.escapeHtml(title) + ": "
                + PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(value)) + "</span>";
    }

    private static String renderCard(String title, String value) {
        return "<div class=\"card\"><strong>" + PepeniumReportSupport.escapeHtml(title) + "</strong><div class=\"value\">"
                + PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(value)) + "</div></div>";
    }

    private static String renderArtifactLink(String label, String href) {
        return "<a class=\"artifact-link\" href=\"" + PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(href))
                + "\"><span>" + PepeniumReportSupport.escapeHtml(label) + "</span><span class=\"muted\">Open</span></a>";
    }

    private static String renderScreenshotAttachment(String label, String href) {
        String safeHref = PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(href));
        return "<div class=\"attachment\"><div><span class=\"badge badge-screenshot\">SCREENSHOT</span></div>"
                + "<div class=\"timeline-message\">" + PepeniumReportSupport.escapeHtml(label) + "</div>"
                + "<a href=\"" + safeHref + "\">Open screenshot</a>"
                + "<img class=\"thumb\" src=\"" + safeHref + "\" alt=\"" + PepeniumReportSupport.escapeHtml(label) + "\">"
                + "<div class=\"path\">" + safeHref + "</div></div>";
    }

    private static String renderFocusItem(String label, String value) {
        return "<div class=\"focus-item\"><div class=\"focus-label\">" + PepeniumReportSupport.escapeHtml(label)
                + "</div><div class=\"focus-value\">" + PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(value))
                + "</div></div>";
    }

    private static String renderStoryMetric(String title, String value) {
        return "<div class=\"story-metric\"><div class=\"story-metric-label\">" + PepeniumReportSupport.escapeHtml(title)
                + "</div><div class=\"story-metric-value\">" + PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(value))
                + "</div></div>";
    }

    private static String renderKeyValue(String title, String value) {
        return "<div class=\"key\">" + PepeniumReportSupport.escapeHtml(title) + "</div><div>"
                + PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(value)) + "</div>";
    }

    private static String renderFlowBlock(PepeniumHtmlReportWriter.FlowBlock block) {
        long blockDurationMillis = Math.max(0L, block.finishedEpochMillis - block.startedEpochMillis);
        StringBuilder html = new StringBuilder();
        html.append("<article class=\"block-card\"><div class=\"block-head\"><div><div class=\"block-title\">")
                .append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(block.title)))
                .append("</div><div class=\"block-subtitle\">Started ")
                .append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(block.startedAt)))
                .append(" · ")
                .append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.formatDurationMillis(blockDurationMillis)))
                .append("</div></div><div class=\"block-kpis\">")
                .append(renderChip(block.events.size() + (block.events.size() == 1 ? " event" : " events")))
                .append(renderChip(block.actionCount + (block.actionCount == 1 ? " action" : " actions")))
                .append(renderChip(block.waitCount + (block.waitCount == 1 ? " wait" : " waits")))
                .append(renderChip(block.assertionCount + (block.assertionCount == 1 ? " assertion" : " assertions")))
                .append("</div></div>");
        if (!keyMessages(block).isEmpty()) {
            html.append("<div class=\"inline-list\">");
            for (String message : keyMessages(block)) {
                html.append("<div class=\"inline-item\">").append(PepeniumReportSupport.escapeHtml(message)).append("</div>");
            }
            html.append("</div>");
        }
        if (!waitSummaries(block).isEmpty()) {
            html.append("<details class=\"compact-details\"><summary>Wait summaries (")
                    .append(waitSummaries(block).size()).append(")</summary><div class=\"list\" style=\"margin-top:12px;\">");
            for (String waitSummary : waitSummaries(block)) {
                html.append("<div class=\"list-item\">").append(PepeniumReportSupport.escapeHtml(waitSummary)).append("</div>");
            }
            html.append("</div></details>");
        }
        html.append("</article>");
        return html.toString();
    }

    private static String renderWaitHotspot(PepeniumHtmlReportWriter.WaitHotspot hotspot) {
        return "<div class=\"list-item\"><strong>" + PepeniumReportSupport.escapeHtml(hotspot.message)
                + "</strong><div class=\"path\">Seen " + hotspot.count + (hotspot.count == 1 ? " time" : " times")
                + " · first " + PepeniumReportSupport.escapeHtml(hotspot.firstSeen)
                + " · last " + PepeniumReportSupport.escapeHtml(hotspot.lastSeen) + "</div></div>";
    }

    private static String renderTimelineCard(PepeniumTimeline.Event anchor, Instant startedAt,
                                             PepeniumHtmlReportWriter.EventGroup group,
                                             java.nio.file.Path reportDir) {
        StringBuilder html = new StringBuilder();
        html.append("<article class=\"timeline-card ")
                .append(timelineCardClass(anchor))
                .append("\"><div class=\"timeline-head\">")
                .append(renderEventTypeBadge(anchor))
                .append(renderEventStatusBadge(anchor))
                .append("<span class=\"timeline-time\">").append(PepeniumReportSupport.escapeHtml(anchor.getTime())).append("</span>")
                .append("</div><div class=\"timeline-meta\">")
                .append("<span class=\"timeline-pill\">+").append(PepeniumReportSupport.escapeHtml(formatElapsed(startedAt, anchor.getEpochMillis()))).append("</span>");
        if (group != null) {
            html.append("<span class=\"timeline-pill\">dt ").append(PepeniumReportSupport.escapeHtml(formatDelta(group))).append("</span>");
        }
        html.append("</div><div class=\"timeline-message\">").append(PepeniumReportSupport.escapeHtml(anchor.getMessage())).append("</div>");
        if (group != null && !group.screenshots.isEmpty()) {
            html.append("<details><summary>Show ")
                    .append(group.screenshots.size())
                    .append(group.screenshots.size() == 1 ? " screenshot" : " screenshots")
                    .append("</summary><div class=\"attachments\">");
            for (PepeniumTimeline.Event screenshot : group.screenshots) {
                String screenshotUri = PepeniumReportSupport.pathToHref(screenshot.getScreenshotPath(), reportDir);
                html.append("<div class=\"attachment\"><div>")
                        .append(renderEventTypeBadge(screenshot))
                        .append("<span class=\"timeline-time\">").append(PepeniumReportSupport.escapeHtml(screenshot.getTime())).append("</span></div>")
                        .append("<div class=\"timeline-message\">").append(PepeniumReportSupport.escapeHtml(screenshot.getMessage())).append("</div>")
                        .append("<a href=\"").append(PepeniumReportSupport.escapeHtml(screenshotUri)).append("\">Open screenshot</a>")
                        .append("<img class=\"thumb\" src=\"").append(PepeniumReportSupport.escapeHtml(screenshotUri)).append("\" alt=\"Screenshot preview\">")
                        .append("<div class=\"path\">").append(PepeniumReportSupport.escapeHtml(screenshot.getScreenshotPath())).append("</div></div>");
            }
            html.append("</div></details>");
        }
        html.append("</article>");
        return html.toString();
    }

    private static List<String> manualScreenshotHrefs(PepeniumHtmlReportWriter.ReportContext report) {
        Set<String> hrefs = new LinkedHashSet<>();
        for (PepeniumHtmlReportWriter.EventGroup group : report.eventGroups) {
            for (PepeniumTimeline.Event screenshot : group.screenshots) {
                String screenshotHref = PepeniumReportSupport.pathToHref(screenshot.getScreenshotPath(), report.reportDir);
                if (screenshotHref != null && !screenshotHref.isBlank()) {
                    hrefs.add(screenshotHref);
                }
            }
        }
        return new ArrayList<>(hrefs);
    }

    private static String renderChip(String value) {
        return "<span class=\"chip\">" + PepeniumReportSupport.escapeHtml(value) + "</span>";
    }

    private static java.util.List<String> keyMessages(PepeniumHtmlReportWriter.FlowBlock block) {
        java.util.List<String> messages = new java.util.ArrayList<>();
        for (PepeniumTimeline.Event event : block.events) {
            if (isKeyTimelineEvent(event) && event.getMessage() != null && messages.size() < 4) {
                messages.add(event.getMessage());
            }
        }
        return messages;
    }

    private static java.util.List<String> waitSummaries(PepeniumHtmlReportWriter.FlowBlock block) {
        java.util.List<String> summaries = new java.util.ArrayList<>();
        for (PepeniumTimeline.Event event : block.events) {
            if (event.getType() == PepeniumTimeline.EventType.WAIT && event.getMessage() != null && summaries.size() < 5) {
                summaries.add(event.getMessage());
            }
        }
        return summaries;
    }

    private static boolean isKeyTimelineEvent(PepeniumTimeline.Event event) {
        return event.getType() == PepeniumTimeline.EventType.STEP
                || event.getType() == PepeniumTimeline.EventType.ASSERT
                || event.getType() == PepeniumTimeline.EventType.ERROR
                || event.getType() == PepeniumTimeline.EventType.SCREENSHOT;
    }

    private static java.util.List<PepeniumTimeline.Event> keyTimelineEvents(PepeniumHtmlReportWriter.ReportContext report) {
        java.util.List<PepeniumTimeline.Event> events = new java.util.ArrayList<>();
        for (PepeniumTimeline.Event event : report.timelineSnapshot.getEvents()) {
            if (isKeyTimelineEvent(event)) {
                events.add(event);
            }
        }
        return events;
    }

    private static String remoteProviderLabel(PepeniumHtmlReportWriter.ReportContext report) {
        return report.remoteContext.enabled
                ? PepeniumReportSupport.defaultValue(report.remoteContext.provider)
                : "Local";
    }

    private static String finalLocation(PepeniumHtmlReportWriter.ReportContext report) {
        if (report.mobileActivity != null || report.mobilePackage != null) {
            return PepeniumReportSupport.defaultValue(report.mobilePackage) + " / "
                    + PepeniumReportSupport.defaultValue(report.mobileActivity);
        }
        if (report.currentUrl != null) {
            return report.currentUrl;
        }
        return "Not captured";
    }

    private static String executionStory(PepeniumHtmlReportWriter.ReportContext report) {
        StringBuilder story = new StringBuilder();
        story.append("This test ");
        story.append(report.failed ? "failed" : "passed");
        story.append(" after ");
        story.append(report.totalEvents);
        story.append(report.totalEvents == 1 ? " event" : " events");
        story.append(" in ");
        story.append(report.duration);
        story.append(". ");
        if (!report.flowBlocks.isEmpty()) {
            story.append("The main flow was condensed into ").append(report.flowBlocks.size())
                    .append(report.flowBlocks.size() == 1 ? " execution block" : " execution blocks").append(". ");
        }
        if (report.actionCount > 0) {
            story.append("It performed ").append(report.actionCount).append(report.actionCount == 1 ? " action" : " actions").append(". ");
        }
        if (report.waitCount > 0) {
            story.append("It recorded ").append(report.waitCount).append(report.waitCount == 1 ? " wait" : " waits").append(". ");
        }
        if (report.keyEventCount > 0) {
            story.append("Only ").append(report.keyEventCount)
                    .append(report.keyEventCount == 1 ? " key event" : " key events")
                    .append(" are highlighted in the compact timeline view. ");
        }
        if (report.passedAssertions + report.failedAssertions > 0) {
            story.append("Assertions: ").append(report.passedAssertions).append(" passed and ")
                    .append(report.failedAssertions).append(" failed. ");
        }
        if (report.lastStep != null) {
            story.append("Last tracked step: ").append(report.lastStep).append(". ");
        }
        if (report.remoteContext.enabled) {
            story.append("Execution used ").append(PepeniumReportSupport.defaultValue(report.remoteContext.provider))
                    .append(" as remote provider.");
        }
        return story.toString().trim();
    }

    private static String failureStory(PepeniumHtmlReportWriter.ReportContext report) {
        StringBuilder story = new StringBuilder();
        story.append("The test ended with ");
        story.append(PepeniumReportSupport.defaultValue(report.rootType));
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

    private static String primaryClue(PepeniumHtmlReportWriter.ReportContext report) {
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

    private static String nextArtifact(PepeniumHtmlReportWriter.ReportContext report) {
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

    private static String executionFingerprint(PepeniumHtmlReportWriter.ReportContext report) {
        StringBuilder value = new StringBuilder();
        value.append(PepeniumReportSupport.defaultValue(report.target)).append(" / ")
                .append(PepeniumReportSupport.defaultValue(report.profileId))
                .append(" / ").append(PepeniumReportSupport.defaultValue(report.driverType));
        if (report.remoteContext.enabled) {
            value.append(" / ").append(PepeniumReportSupport.defaultValue(report.remoteContext.provider));
        }
        return value.toString();
    }

    private static String renderEventTypeBadge(PepeniumTimeline.Event event) {
        return "<span class=\"badge " + eventTypeBadgeClass(event.getType()) + "\">"
                + PepeniumReportSupport.escapeHtml(event.getType().name()) + "</span>";
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

    private static String formatElapsed(Instant startedAt, long epochMillis) {
        return PepeniumReportSupport.formatDurationMillis(epochMillis - startedAt.toEpochMilli());
    }

    private static String formatDelta(PepeniumHtmlReportWriter.EventGroup group) {
        PepeniumTimeline.Event anchor = group.anchorEvent;
        if (group.previousAnchorEpochMillis <= 0L) {
            return "start";
        }
        return PepeniumReportSupport.formatDurationMillis(anchor.getEpochMillis() - group.previousAnchorEpochMillis);
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
}
