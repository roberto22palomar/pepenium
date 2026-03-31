package io.github.roberto22palomar.pepenium.core.observability;

import java.time.Instant;
import java.time.ZoneId;

final class PepeniumReportHtmlRenderer {

    private PepeniumReportHtmlRenderer() {
    }

    static String render(PepeniumHtmlReportWriter.ReportContext report) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>").append(PepeniumReportSupport.escapeHtml(report.testName)).append("</title>")
                .append("<style>")
                .append(":root{--bg:#f5f7fb;--surface:#ffffff;--surface-alt:#f9fbff;--border:#d8dee9;--text:#1f2937;--muted:#5b6472;--pass:#117a37;--pass-bg:#dafbe1;--fail:#cf222e;--fail-bg:#ffebe9;--action:#0550ae;--action-bg:#ddf4ff;--wait:#7c3aed;--wait-bg:#f3e8ff;--assert:#9a6700;--assert-bg:#fff8c5;--shot:#8250df;--shot-bg:#fbefff;--error:#b42318;--error-bg:#ffe4e6;}")
                .append("*{box-sizing:border-box;}body{margin:0;font-family:Segoe UI,Arial,sans-serif;background:radial-gradient(circle at top,#edf4ff 0,#f5f7fb 35%,#f7f8fb 100%);color:var(--text);}a{color:#0969da;text-decoration:none;}a:hover{text-decoration:underline;}")
                .append(".wrap{max-width:1240px;margin:0 auto;padding:28px 20px 48px;} .hero{background:linear-gradient(135deg,#ffffff 0,#f6faff 100%);border:1px solid var(--border);border-radius:24px;padding:28px;box-shadow:0 18px 40px rgba(24,39,75,.08);} .hero-top{display:flex;justify-content:space-between;gap:16px;align-items:flex-start;flex-wrap:wrap;} .hero h1{margin:10px 0 8px;font-size:30px;line-height:1.15;} .hero-meta{display:flex;flex-wrap:wrap;gap:14px;margin-top:10px;font-size:14px;color:var(--muted);} .hero-actions{display:flex;gap:10px;flex-wrap:wrap;}")
                .append(".status{display:inline-flex;align-items:center;gap:8px;padding:8px 12px;border-radius:999px;font-size:12px;font-weight:700;letter-spacing:.04em;text-transform:uppercase;} .status.passed{background:var(--pass-bg);color:var(--pass);} .status.failed{background:var(--fail-bg);color:var(--fail);} .pill{display:inline-flex;align-items:center;padding:8px 12px;border-radius:999px;background:#eef5ff;color:#244f8f;font-size:12px;font-weight:700;}")
                .append(".metrics{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:12px;margin-top:20px;} .metric,.card,.panel,.timeline-card,.artifact-card{background:var(--surface);border:1px solid var(--border);border-radius:18px;box-shadow:0 10px 24px rgba(16,24,40,.04);} .metric{padding:16px 18px;background:linear-gradient(180deg,#ffffff 0,#fbfdff 100%);} .metric-label{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;} .metric-value{margin-top:8px;font-size:22px;font-weight:700;}")
                .append(".section{margin-top:24px;} .section h2{margin:0 0 12px;font-size:18px;} .grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:16px;} .card,.panel,.artifact-card,.story-card{padding:18px;} .card strong{display:block;font-size:12px;text-transform:uppercase;letter-spacing:.05em;color:var(--muted);margin-bottom:8px;} .value{font-size:15px;line-height:1.45;word-break:break-word;}")
                .append(".keyvals{display:grid;grid-template-columns:minmax(120px,180px) 1fr;gap:10px 16px;align-items:start;} .key{font-weight:700;color:#374151;} .keyvals div{word-break:break-word;} .report-layout{display:grid;grid-template-columns:minmax(0,1.3fr) minmax(300px,.7fr);gap:18px;margin-top:24px;} .sidebar{display:flex;flex-direction:column;gap:0;align-self:start;position:sticky;top:18px;} @media (max-width:980px){.report-layout{grid-template-columns:1fr;}.sidebar{position:static;}}")
                .append(".badge{display:inline-flex;align-items:center;gap:5px;padding:3px 8px;border-radius:999px;font-size:10px;font-weight:700;letter-spacing:.03em;text-transform:uppercase;margin-right:6px;} .badge-step{background:#eef2ff;color:#3730a3;} .badge-action{background:var(--action-bg);color:var(--action);} .badge-wait{background:var(--wait-bg);color:var(--wait);} .badge-assert{background:var(--assert-bg);color:var(--assert);} .badge-screenshot{background:var(--shot-bg);color:var(--shot);} .badge-error{background:var(--error-bg);color:var(--error);} .badge-pass{background:var(--pass-bg);color:var(--pass);} .badge-fail{background:var(--fail-bg);color:var(--fail);} .timeline{display:flex;flex-direction:column;gap:10px;} .timeline-card{padding:12px 14px;background:linear-gradient(180deg,#fff 0,#fbfcff 100%);} .timeline-card.is-failure{border-color:#ef9a9a;box-shadow:0 10px 24px rgba(207,34,46,.12);} .timeline-card.is-warning{border-color:#d8b4fe;box-shadow:0 10px 24px rgba(124,58,237,.10);} .timeline-head{display:flex;gap:8px;align-items:center;flex-wrap:wrap;} .timeline-time{font-size:11px;color:var(--muted);font-weight:700;} .timeline-meta{display:flex;gap:8px;flex-wrap:wrap;margin-top:6px;} .timeline-pill{display:inline-flex;align-items:center;padding:3px 8px;border-radius:999px;background:#eef2f8;color:#475467;font-size:10px;font-weight:700;} .timeline-message{margin-top:6px;font-size:14px;line-height:1.4;}")
                .append(".attachments{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:10px;margin-top:10px;} .attachment{background:var(--surface-alt);border:1px dashed #c6d1e1;border-radius:12px;padding:10px;} .thumb{display:block;width:100%;max-height:180px;object-fit:cover;border-radius:10px;border:1px solid var(--border);margin-top:8px;background:#fff;} .path{margin-top:6px;font-size:11px;color:var(--muted);word-break:break-word;}")
                .append(".list{display:flex;flex-direction:column;gap:10px;} .list-item{padding:12px 14px;border-radius:14px;background:var(--surface-alt);border:1px solid #e3e8f1;font-size:14px;line-height:1.45;} .stack{margin-top:12px;} details summary{cursor:pointer;font-weight:700;} pre{margin:10px 0 0;padding:16px;background:#0f172a;color:#e2e8f0;border-radius:16px;overflow:auto;white-space:pre-wrap;word-break:break-word;} .empty{padding:22px;border-radius:16px;border:1px dashed #c6d1e1;color:var(--muted);background:#fbfcfe;} .story-card{background:linear-gradient(135deg,#ffffff 0,#f7fbff 100%);border:1px solid var(--border);border-radius:18px;box-shadow:0 12px 24px rgba(16,24,40,.04);} .story-title{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;font-weight:700;margin-bottom:10px;} .story-body{font-size:15px;line-height:1.6;} .artifact-list,.focus-list{display:flex;flex-direction:column;gap:10px;} .artifact-link,.focus-item{display:flex;justify-content:space-between;align-items:center;gap:12px;padding:12px 14px;border-radius:14px;background:var(--surface-alt);border:1px solid #e3e8f1;font-size:14px;} .focus-item{align-items:flex-start;flex-direction:column;} .focus-label{font-size:12px;color:var(--muted);text-transform:uppercase;letter-spacing:.05em;font-weight:700;} .focus-value{font-size:14px;line-height:1.5;word-break:break-word;}")
                .append("</style></head><body><div class=\"wrap\">");

        html.append("<section class=\"hero\"><div class=\"hero-top\"><div><span class=\"status ")
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
                .append(renderMetric("Assertions", report.passedAssertions + " / " + report.failedAssertions))
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
                html.append("<div class=\"list-item\">").append(PepeniumReportSupport.escapeHtml(step)).append("</div>");
            }
            html.append("</div>");
        }
        html.append("</div></section>");

        html.append("<section class=\"section\"><h2>Timeline</h2><div class=\"timeline\">");
        if (report.eventGroups.isEmpty()) {
            html.append("<div class=\"empty\">No timeline events were recorded for this test.</div>");
        } else {
            for (PepeniumHtmlReportWriter.EventGroup group : report.eventGroups) {
                PepeniumTimeline.Event anchor = group.anchorEvent;
                html.append("<article class=\"timeline-card ")
                        .append(timelineCardClass(anchor))
                        .append("\"><div class=\"timeline-head\">")
                        .append(renderEventTypeBadge(anchor))
                        .append(renderEventStatusBadge(anchor))
                        .append("<span class=\"timeline-time\">").append(PepeniumReportSupport.escapeHtml(anchor.getTime())).append("</span>")
                        .append("</div><div class=\"timeline-meta\">")
                        .append("<span class=\"timeline-pill\">+").append(PepeniumReportSupport.escapeHtml(formatElapsed(report.startedAt, anchor.getEpochMillis()))).append("</span>")
                        .append("<span class=\"timeline-pill\">dt ").append(PepeniumReportSupport.escapeHtml(formatDelta(group))).append("</span>")
                        .append("</div><div class=\"timeline-message\">").append(PepeniumReportSupport.escapeHtml(anchor.getMessage())).append("</div>");
                if (!group.screenshots.isEmpty()) {
                    html.append("<details><summary>Show ")
                            .append(group.screenshots.size())
                            .append(group.screenshots.size() == 1 ? " screenshot" : " screenshots")
                            .append("</summary><div class=\"attachments\">");
                    for (PepeniumTimeline.Event screenshot : group.screenshots) {
                        String screenshotUri = PepeniumReportSupport.pathToUri(screenshot.getScreenshotPath());
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
            }
        }
        html.append("</div></section>");

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

        html.append("<section class=\"section\"><h2>Artifacts</h2><div class=\"artifact-card\"><div class=\"artifact-list\">")
                .append(renderArtifactLink("Suite index", "index.html"))
                .append(renderArtifactLink("Suite summary JSON", "summary.json"));
        if (report.screenshotUri != null) {
            html.append(renderArtifactLink("Final screenshot", report.screenshotUri));
        }
        if (report.lastScreenshotPath != null) {
            html.append(renderArtifactLink("Last manual screenshot", PepeniumReportSupport.pathToUri(report.lastScreenshotPath)));
        }
        if (report.remoteContext.enabled && report.remoteContext.dashboardUrl != null) {
            html.append(renderArtifactLink("Remote dashboard", report.remoteContext.dashboardUrl));
        }
        html.append("</div></div></section>");

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

    private static String renderFocusItem(String label, String value) {
        return "<div class=\"focus-item\"><div class=\"focus-label\">" + PepeniumReportSupport.escapeHtml(label)
                + "</div><div class=\"focus-value\">" + PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(value))
                + "</div></div>";
    }

    private static String renderKeyValue(String title, String value) {
        return "<div class=\"key\">" + PepeniumReportSupport.escapeHtml(title) + "</div><div>"
                + PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(value)) + "</div>";
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
