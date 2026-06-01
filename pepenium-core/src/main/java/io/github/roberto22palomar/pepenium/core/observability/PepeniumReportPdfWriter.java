package io.github.roberto22palomar.pepenium.core.observability;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

final class PepeniumReportPdfWriter {

    static final String SUMMARY_PDF_FILE = "execution-summary.pdf";

    private static final float MARGIN = 42f;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final Color INK = new Color(17, 24, 39);
    private static final Color MUTED = new Color(102, 112, 133);
    private static final Color BORDER = new Color(216, 222, 233);
    private static final Color SOFT_BLUE = new Color(239, 246, 255);
    private static final Color PASS = new Color(17, 122, 55);
    private static final Color FAIL = new Color(207, 34, 46);
    private static final PDFont FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private PepeniumReportPdfWriter() {
    }

    static Path writeSummaryPdf(Path reportDir, List<PepeniumHtmlReportWriter.ReportSummary> summaries)
            throws IOException {
        Path pdfFile = reportDir.resolve(SUMMARY_PDF_FILE);
        Path temporaryPdfFile = reportDir.resolve(SUMMARY_PDF_FILE + ".tmp");
        List<PdfEntry> entries = summaries.stream()
                .map(summary -> PdfEntry.from(reportDir, summary))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        try (PDDocument document = new PDDocument()) {
            PdfCanvas canvas = new PdfCanvas(document);
            writeCover(canvas, summaries);
            writeResults(canvas, entries);
            writeScreenshots(canvas, entries);
            canvas.close();
            document.save(temporaryPdfFile.toFile());
        }
        Files.move(temporaryPdfFile, pdfFile, StandardCopyOption.REPLACE_EXISTING);
        return pdfFile;
    }

    private static void writeCover(PdfCanvas canvas, List<PepeniumHtmlReportWriter.ReportSummary> summaries)
            throws IOException {
        long passed = summaries.stream().filter(summary -> "PASSED".equals(summary.outcome)).count();
        long failed = summaries.size() - passed;
        long totalDuration = summaries.stream().mapToLong(summary -> summary.durationMillis).sum();
        int passRate = summaries.isEmpty() ? 0 : (int) Math.round((passed * 100.0d) / summaries.size());

        canvas.newPage();
        canvas.title("Pepenium Execution Summary");
        canvas.subtitle("Generated " + Instant.now());
        canvas.text("Run", PepeniumReportRun.id(), 10, MUTED);
        canvas.text("Started", PepeniumReportRun.startedAt().toString(), 10, MUTED);

        canvas.moveDown(18);
        float cardWidth = (PAGE_WIDTH - MARGIN * 2 - 24) / 3;
        canvas.metricCard("Result", failed == 0 ? "PASSED" : "FAILED", failed == 0 ? PASS : FAIL, cardWidth);
        canvas.metricCard("Tests", summaries.size() + " total", INK, cardWidth);
        canvas.metricCard("Pass rate", passRate + "%", passRate == 100 ? PASS : FAIL, cardWidth);
        canvas.moveDown(96);
        canvas.metricCard("Passed", String.valueOf(passed), PASS, cardWidth);
        canvas.metricCard("Failed", String.valueOf(failed), failed == 0 ? PASS : FAIL, cardWidth);
        canvas.metricCard("Duration", PepeniumReportSupport.formatDurationMillis(totalDuration), INK, cardWidth);

        canvas.moveDown(120);
        canvas.section("Executive read");
        if (summaries.isEmpty()) {
            canvas.paragraph("No reports were generated in this execution.", 12, MUTED, PAGE_WIDTH - MARGIN * 2);
        } else if (failed == 0) {
            canvas.paragraph("All automated checks included in this run finished successfully. "
                    + "The detailed HTML report remains available for drill-down analysis.", 12, INK, PAGE_WIDTH - MARGIN * 2);
        } else {
            canvas.paragraph("The execution completed with failing checks. Review the failed tests and their screenshots "
                    + "before sending final validation status.", 12, INK, PAGE_WIDTH - MARGIN * 2);
        }
    }

    private static void writeResults(PdfCanvas canvas, List<PdfEntry> entries) throws IOException {
        canvas.newPage();
        canvas.section("Test results");
        canvas.paragraph("Compact list of executed tests. Detailed technical diagnostics remain in the HTML artifacts.",
                11, MUTED, PAGE_WIDTH - MARGIN * 2);
        canvas.moveDown(14);

        canvas.tableHeader("Status", "Test", "Duration", "Evidence");
        for (PdfEntry entry : entries) {
            if (canvas.y < 86) {
                canvas.newPage();
                canvas.section("Test results");
                canvas.tableHeader("Status", "Test", "Duration", "Evidence");
            }
            canvas.tableRow(
                    entry.summary.outcome,
                    entry.summary.testName,
                    entry.summary.durationDisplay,
                    entry.screenshotPaths.isEmpty() ? "No screenshot" : entry.screenshotPaths.size() + " screenshot(s)",
                    "PASSED".equals(entry.summary.outcome) ? PASS : FAIL
            );
        }
    }

    private static void writeScreenshots(PdfCanvas canvas, List<PdfEntry> entries) throws IOException {
        List<PdfEntry> withScreenshots = entries.stream()
                .filter(entry -> !entry.screenshotPaths.isEmpty())
                .sorted(Comparator.comparing((PdfEntry entry) -> "PASSED".equals(entry.summary.outcome)))
                .limit(8)
                .collect(Collectors.toList());

        if (withScreenshots.isEmpty()) {
            return;
        }

        canvas.newPage();
        canvas.section("Screenshot evidence");
        canvas.paragraph("Representative screenshots captured during the execution.", 11, MUTED, PAGE_WIDTH - MARGIN * 2);
        canvas.moveDown(12);

        for (PdfEntry entry : withScreenshots) {
            if (canvas.y < 250) {
                canvas.newPage();
                canvas.section("Screenshot evidence");
            }
            canvas.screenshotBlock(entry);
        }
    }

    private static final class PdfEntry {
        final PepeniumHtmlReportWriter.ReportSummary summary;
        final List<Path> screenshotPaths;
        final String pageTitle;
        final String currentUrl;
        final String failureType;
        final String failureMessage;

        private PdfEntry(
                PepeniumHtmlReportWriter.ReportSummary summary,
                List<Path> screenshotPaths,
                String pageTitle,
                String currentUrl,
                String failureType,
                String failureMessage
        ) {
            this.summary = summary;
            this.screenshotPaths = screenshotPaths;
            this.pageTitle = pageTitle;
            this.currentUrl = currentUrl;
            this.failureType = failureType;
            this.failureMessage = failureMessage;
        }

        static PdfEntry from(Path reportDir, PepeniumHtmlReportWriter.ReportSummary summary) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = PepeniumReportSupport.YAML.load(Files.readString(
                        reportDir.resolve(summary.jsonReport),
                        StandardCharsets.UTF_8
                ));
                Map<String, Object> execution = PepeniumReportSupport.mapValue(data.get("execution"));
                Map<String, Object> failure = PepeniumReportSupport.mapValue(data.get("failure"));
                return new PdfEntry(
                        summary,
                        screenshots(reportDir, data),
                        PepeniumReportSupport.safe(execution.get("pageTitle")),
                        PepeniumReportSupport.safe(execution.get("currentUrl")),
                        PepeniumReportSupport.safe(failure.get("rootType")),
                        concise(PepeniumReportSupport.safe(failure.get("rootMessage")), 220)
                );
            } catch (Exception ignored) {
                return new PdfEntry(summary, List.of(), null, null, null, null);
            }
        }

        private static List<Path> screenshots(Path reportDir, Map<String, Object> data) {
            List<Path> screenshots = new ArrayList<>();
            addIfPresent(screenshots, screenshotPath(reportDir, PepeniumReportSupport.safe(
                    PepeniumReportSupport.mapValue(data.get("highlights")).get("lastScreenshotPath")
            )));
            Object eventsObject = data.get("events");
            if (!(eventsObject instanceof List<?>)) {
                return screenshots;
            }
            List<?> events = (List<?>) eventsObject;
            for (Object eventObject : events) {
                if (eventObject instanceof Map<?, ?>) {
                    Map<?, ?> event = (Map<?, ?>) eventObject;
                    String screenshot = PepeniumReportSupport.safe(event.get("screenshotPath"));
                    addIfPresent(screenshots, screenshotPath(reportDir, screenshot));
                }
            }
            return screenshots.stream().distinct().limit(4).collect(Collectors.toList());
        }

        private static void addIfPresent(List<Path> screenshots, Path path) {
            if (path != null) {
                screenshots.add(path);
            }
        }

        private static Path screenshotPath(Path reportDir, String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            Path path = Path.of(value);
            if (path.isAbsolute() && Files.exists(path)) {
                return path;
            }
            if (!path.isAbsolute()) {
                Path fromWorkingDirectory = Path.of("").toAbsolutePath().resolve(path).normalize();
                if (Files.exists(fromWorkingDirectory)) {
                    return fromWorkingDirectory;
                }
                Path fromReportDirectory = reportDir.resolve(path).normalize();
                if (Files.exists(fromReportDirectory)) {
                    return fromReportDirectory;
                }
                Path fromReportScreenshots = reportDir.resolve("screenshots").resolve(path.getFileName()).normalize();
                if (Files.exists(fromReportScreenshots)) {
                    return fromReportScreenshots;
                }
            }
            return null;
        }
    }

    private static final class PdfCanvas {
        final PDDocument document;
        PDPageContentStream stream;
        float y;
        int pageNumber;
        int cardsInRow;

        PdfCanvas(PDDocument document) {
            this.document = document;
        }

        void newPage() throws IOException {
            closeStream();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            pageNumber++;
            y = PAGE_HEIGHT - MARGIN;
            cardsInRow = 0;
            footer();
        }

        void title(String text) throws IOException {
            drawText(text, FONT_BOLD, 26, MARGIN, y, INK);
            y -= 34;
        }

        void subtitle(String text) throws IOException {
            drawText(text, FONT, 11, MARGIN, y, MUTED);
            y -= 20;
        }

        void section(String text) throws IOException {
            drawText(text, FONT_BOLD, 18, MARGIN, y, INK);
            y -= 24;
        }

        void text(String label, String value, float size, Color color) throws IOException {
            drawText(label + ": " + PepeniumReportSupport.defaultValue(value), FONT, size, MARGIN, y, color);
            y -= 16;
        }

        void paragraph(String text, float size, Color color, float width) throws IOException {
            for (String line : wrap(text, FONT, size, width)) {
                drawText(line, FONT, size, MARGIN, y, color);
                y -= size + 5;
            }
        }

        void metricCard(String label, String value, Color valueColor, float width) throws IOException {
            float gap = 12;
            float x = MARGIN + cardsInRow * (width + gap);
            float cardY = y - 72;
            fillRect(x, cardY, width, 72, SOFT_BLUE);
            strokeRect(x, cardY, width, 72, BORDER);
            drawText(label, FONT, 10, x + 12, y - 22, MUTED);
            drawText(value, FONT_BOLD, 18, x + 12, y - 48, valueColor);
            cardsInRow = (cardsInRow + 1) % 3;
        }

        void tableHeader(String col1, String col2, String col3, String col4) throws IOException {
            fillRect(MARGIN, y - 20, PAGE_WIDTH - MARGIN * 2, 24, SOFT_BLUE);
            drawText(col1, FONT_BOLD, 9, MARGIN + 8, y - 12, INK);
            drawText(col2, FONT_BOLD, 9, MARGIN + 76, y - 12, INK);
            drawText(col3, FONT_BOLD, 9, MARGIN + 360, y - 12, INK);
            drawText(col4, FONT_BOLD, 9, MARGIN + 450, y - 12, INK);
            y -= 28;
        }

        void tableRow(String status, String test, String duration, String evidence, Color statusColor) throws IOException {
            strokeRect(MARGIN, y - 24, PAGE_WIDTH - MARGIN * 2, 28, new Color(232, 236, 243));
            drawText(status, FONT_BOLD, 8, MARGIN + 8, y - 12, statusColor);
            drawText(concise(test, 52), FONT, 9, MARGIN + 76, y - 12, INK);
            drawText(PepeniumReportSupport.defaultValue(duration), FONT, 9, MARGIN + 360, y - 12, INK);
            drawText(evidence, FONT, 9, MARGIN + 450, y - 12, MUTED);
            y -= 30;
        }

        void screenshotBlock(PdfEntry entry) throws IOException {
            drawText(entry.summary.outcome + " - " + entry.summary.testName, FONT_BOLD, 12, MARGIN, y, INK);
            y -= 16;
            if (entry.pageTitle != null) {
                drawText(concise(entry.pageTitle, 88), FONT, 9, MARGIN, y, MUTED);
                y -= 13;
            }
            if (entry.failureType != null && !"PASSED".equals(entry.summary.outcome)) {
                drawText(entry.failureType + ": " + PepeniumReportSupport.defaultValue(entry.failureMessage),
                        FONT, 9, MARGIN, y, FAIL);
                y -= 13;
            }
            for (Path screenshotPath : entry.screenshotPaths) {
                drawScreenshot(screenshotPath);
            }
        }

        private void drawScreenshot(Path screenshotPath) throws IOException {
            if (y < 230) {
                newPage();
                section("Screenshot evidence");
            }
            try {
                PDImageXObject image = PDImageXObject.createFromFileByContent(screenshotPath.toFile(), document);
                float maxWidth = PAGE_WIDTH - MARGIN * 2;
                float maxHeight = 180f;
                float scale = Math.min(maxWidth / image.getWidth(), maxHeight / image.getHeight());
                float imageWidth = image.getWidth() * scale;
                float imageHeight = image.getHeight() * scale;
                stream.drawImage(image, MARGIN, y - imageHeight, imageWidth, imageHeight);
                y -= imageHeight + 22;
            } catch (Exception e) {
                drawText("Screenshot could not be embedded: " + screenshotPath.getFileName(),
                        FONT, 9, MARGIN, y, MUTED);
                y -= 22;
            }
        }

        void moveDown(float amount) {
            y -= amount;
            cardsInRow = 0;
        }

        void close() throws IOException {
            closeStream();
        }

        private void footer() throws IOException {
            drawText("Pepenium execution summary", FONT, 8, MARGIN, 22, MUTED);
            drawText("Page " + pageNumber, FONT, 8, PAGE_WIDTH - MARGIN - 34, 22, MUTED);
        }

        private void fillRect(float x, float y, float width, float height, Color color) throws IOException {
            stream.setNonStrokingColor(color);
            stream.addRect(x, y, width, height);
            stream.fill();
        }

        private void strokeRect(float x, float y, float width, float height, Color color) throws IOException {
            stream.setStrokingColor(color);
            stream.addRect(x, y, width, height);
            stream.stroke();
        }

        private void drawText(String text, PDFont font, float size, float x, float y, Color color) throws IOException {
            stream.beginText();
            stream.setFont(font, size);
            stream.setNonStrokingColor(color);
            stream.newLineAtOffset(x, y);
            stream.showText(pdfSafe(text, font));
            stream.endText();
        }

        private void closeStream() throws IOException {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private static List<String> wrap(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = PepeniumReportSupport.defaultValue(text).split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (font.getStringWidth(pdfSafe(candidate, font)) / 1000 * fontSize > maxWidth && line.length() > 0) {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            } else {
                line.setLength(0);
                line.append(candidate);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    private static String concise(String value, int maxLength) {
        String text = PepeniumReportSupport.defaultValue(value).replace('\n', ' ').replace('\r', ' ').trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)).trim() + "...";
    }

    private static String pdfSafe(String value, PDFont font) {
        String text = PepeniumReportSupport.defaultValue(value);
        StringBuilder safe = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            String character = String.valueOf(text.charAt(i));
            try {
                font.encode(character);
                safe.append(character);
            } catch (Exception ignored) {
                safe.append('?');
            }
        }
        return safe.toString();
    }
}
