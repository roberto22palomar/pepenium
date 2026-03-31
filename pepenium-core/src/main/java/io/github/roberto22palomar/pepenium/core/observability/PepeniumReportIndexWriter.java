package io.github.roberto22palomar.pepenium.core.observability;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class PepeniumReportIndexWriter {

    private PepeniumReportIndexWriter() {
    }

    static Path writeIndex(Path reportDir) throws IOException {
        List<Path> jsonFiles;
        try (Stream<Path> files = Files.list(reportDir)) {
            jsonFiles = files
                    .filter(path -> path.getFileName().toString().startsWith("report-"))
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(PepeniumReportSupport::lastModifiedSafely).reversed())
                    .collect(Collectors.toList());
        }

        List<PepeniumHtmlReportWriter.ReportSummary> summaries = new ArrayList<>();
        for (Path jsonFile : jsonFiles) {
            PepeniumHtmlReportWriter.ReportSummary summary = PepeniumReportJsonRenderer.loadSummary(jsonFile);
            if (summary != null) {
                summaries.add(summary);
            }
        }

        Files.writeString(
                reportDir.resolve("summary.json"),
                PepeniumReportJsonRenderer.renderSuiteSummaryJson(summaries),
                StandardCharsets.UTF_8
        );
        Path indexFile = reportDir.resolve("index.html");
        Files.writeString(indexFile, renderIndexHtml(reportDir, summaries), StandardCharsets.UTF_8);
        return indexFile;
    }

    private static String renderIndexHtml(Path reportDir, List<PepeniumHtmlReportWriter.ReportSummary> summaries) {
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
                .append(PepeniumReportSupport.escapeHtml(reportDir.toAbsolutePath().toString()))
                .append("</p><div class=\"metrics\">")
                .append(renderMetric("Total Reports", String.valueOf(summaries.size())))
                .append(renderMetric("Passed", String.valueOf(passedCount)))
                .append(renderMetric("Failed", String.valueOf(failedCount)))
                .append(renderMetric("Remote Runs", String.valueOf(remoteRuns)))
                .append(renderMetric("Total Duration", PepeniumReportSupport.formatDurationMillis(totalDuration)))
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
            for (PepeniumHtmlReportWriter.ReportSummary summary : summaries) {
                String provider = PepeniumReportSupport.defaultValue(summary.provider);
                String searchText = (
                        PepeniumReportSupport.defaultValue(summary.testName) + " " +
                        PepeniumReportSupport.defaultValue(summary.profileId) + " " +
                        PepeniumReportSupport.defaultValue(summary.target) + " " +
                        PepeniumReportSupport.defaultValue(summary.driverType) + " " +
                        provider
                ).toLowerCase(Locale.ROOT);
                html.append("<article class=\"report-row\" data-search=\"")
                        .append(PepeniumReportSupport.escapeHtml(searchText))
                        .append("\" data-status=\"").append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(summary.outcome)))
                        .append("\" data-target=\"").append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(summary.target)))
                        .append("\" data-profile=\"").append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(summary.profileId)))
                        .append("\" data-provider=\"").append(PepeniumReportSupport.escapeHtml(provider))
                        .append("\"><div class=\"row-top\"><div><div class=\"row-title\"><a href=\"")
                        .append(PepeniumReportSupport.escapeHtml(summary.htmlReport))
                        .append("\">").append(PepeniumReportSupport.escapeHtml(summary.testName)).append("</a></div>")
                        .append("<div class=\"row-meta\"><span class=\"badge ")
                        .append("PASSED".equals(summary.outcome) ? "passed" : "failed")
                        .append("\">").append(PepeniumReportSupport.escapeHtml(summary.outcome)).append("</span>")
                        .append("<span class=\"meta\">").append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(summary.target))).append("</span>")
                        .append("<span class=\"meta\">").append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(summary.profileId))).append("</span>")
                        .append("<span class=\"meta\">").append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(summary.driverType))).append("</span>")
                        .append("</div></div><div class=\"row-bottom\"><span class=\"meta\">")
                        .append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(summary.durationDisplay))).append("</span><span class=\"muted small\">")
                        .append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(summary.generatedAt))).append("</span></div></div>")
                        .append("<div class=\"row-tags\"><span class=\"meta\">Provider: ")
                        .append(PepeniumReportSupport.escapeHtml(provider)).append("</span><span class=\"meta\">Screenshots: ")
                        .append(summary.screenshotCount).append("</span><a class=\"meta\" href=\"")
                        .append(PepeniumReportSupport.escapeHtml(summary.jsonReport)).append("\">JSON</a></div></article>");
            }
        }
        html.append("</div></section></div></body></html>");
        return html.toString();
    }

    private static String renderMetric(String title, String value) {
        return "<div class=\"metric\"><div class=\"metric-label\">" + PepeniumReportSupport.escapeHtml(title)
                + "</div><div class=\"metric-value\">" + PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(value))
                + "</div></div>";
    }

    private static List<String> uniqueValues(
            List<PepeniumHtmlReportWriter.ReportSummary> summaries,
            PepeniumHtmlReportWriter.SummarySelector selector
    ) {
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
        html.append("<select id=\"").append(id).append("\" aria-label=\"").append(PepeniumReportSupport.escapeHtml(label))
                .append("\" onchange=\"applyFilters()\"><option value=\"\">")
                .append(PepeniumReportSupport.escapeHtml(label)).append(": all</option>");
        for (String value : values) {
            html.append("<option value=\"").append(PepeniumReportSupport.escapeHtml(value)).append("\">")
                    .append(PepeniumReportSupport.escapeHtml(value)).append("</option>");
        }
        html.append("</select>");
        return html.toString();
    }

    private static String renderBreakdownPanel(String title, Map<String, Long> counts) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"panel\"><h3>").append(PepeniumReportSupport.escapeHtml(title)).append("</h3>");
        if (counts.isEmpty()) {
            html.append("<p class=\"muted\">No data available yet.</p>");
        } else {
            html.append("<div class=\"tags\">");
            counts.forEach((label, count) -> html.append("<span class=\"tag\">")
                    .append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(label)))
                    .append(" - ")
                    .append(count)
                    .append("</span>"));
            html.append("</div>");
        }
        html.append("</div>");
        return html.toString();
    }

    private static String renderTopListPanel(String title, List<PepeniumHtmlReportWriter.ReportSummary> summaries) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"panel\"><h3>").append(PepeniumReportSupport.escapeHtml(title)).append("</h3>");
        if (summaries.isEmpty()) {
            html.append("<p class=\"muted\">No data available yet.</p>");
        } else {
            html.append("<div class=\"ranking\">");
            for (PepeniumHtmlReportWriter.ReportSummary summary : summaries) {
                html.append("<div class=\"ranking-item\"><a href=\"")
                        .append(PepeniumReportSupport.escapeHtml(summary.htmlReport))
                        .append("\">")
                        .append(PepeniumReportSupport.escapeHtml(PepeniumReportSupport.defaultValue(summary.testName)))
                        .append("</a><span class=\"meta\">")
                        .append(PepeniumReportSupport.escapeHtml(topMetric(summary, title)))
                        .append("</span></div>");
            }
            html.append("</div>");
        }
        html.append("</div>");
        return html.toString();
    }

    private static String topMetric(PepeniumHtmlReportWriter.ReportSummary summary, String title) {
        if ("Most Screenshots".equals(title)) {
            return summary.screenshotCount + " screenshots";
        }
        return PepeniumReportSupport.defaultValue(summary.durationDisplay);
    }

    private static List<PepeniumHtmlReportWriter.ReportSummary> topSlowest(List<PepeniumHtmlReportWriter.ReportSummary> summaries) {
        return summaries.stream()
                .sorted(Comparator.comparingLong((PepeniumHtmlReportWriter.ReportSummary summary) -> summary.durationMillis).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private static List<PepeniumHtmlReportWriter.ReportSummary> topScreenshots(List<PepeniumHtmlReportWriter.ReportSummary> summaries) {
        return summaries.stream()
                .sorted(Comparator.comparingLong((PepeniumHtmlReportWriter.ReportSummary summary) -> summary.screenshotCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private static Map<String, Long> groupCounts(
            List<PepeniumHtmlReportWriter.ReportSummary> summaries,
            PepeniumHtmlReportWriter.SummarySelector selector
    ) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (PepeniumHtmlReportWriter.ReportSummary summary : summaries) {
            String value = PepeniumReportSupport.defaultValue(selector.get(summary));
            counts.put(value, counts.getOrDefault(value, 0L) + 1);
        }
        return counts;
    }
}
