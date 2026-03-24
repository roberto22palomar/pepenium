package io.github.roberto22palomar.pepenium.toolkit.utils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import io.github.roberto22palomar.pepenium.core.observability.LoggingPreferences;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Slf4j
public class ActionsWeb {

    private final WebDriver driver;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(6L);
    private static final Duration LONG_TIMEOUT = Duration.ofSeconds(120L);
    private static final Duration SCREENSHOT_SETTLE_TIMEOUT = Duration.ofMillis(700);
    private static final Duration SCREENSHOT_SETTLE_POLL = Duration.ofMillis(100);
    private static final Duration POST_CLICK_SETTLE_TIMEOUT = Duration.ofMillis(500);

    private final By openOverlay = By.cssSelector("[data-slot='sheet-overlay'][data-state='open']");
    private final By closeSheetButton = By.cssSelector(
            "[data-slot='sheet-close'], [data-state='open'] [aria-label='Close'], button[data-slot='sheet-close']"
    );

    public void waitForOpenOverlay() {
        new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.visibilityOfElementLocated(openOverlay));
    }

    public void closeSheetIfOpen() {
        List<WebElement> open = driver.findElements(openOverlay);
        if (!open.isEmpty()) {
            StepTracker.record("Close open sheet overlay");
            try {
                click(closeSheetButton);
            } catch (Exception ignore) {
                new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            }
            new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(d -> d.findElements(openOverlay).isEmpty());
        }
    }

    public void waitForAtLeastNElements(By locator, int n) {
        new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(d -> d.findElements(locator).size() >= n);
    }

    public WebElement waitToBeVisible(By locator) {
        try {
            return new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            log.error("Timeout waiting for element visibility: {}", locator);
            LoggingPreferences.logDetail(log, "Visibility wait stacktrace", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while waiting for element visibility: {} ({})", locator, e.getClass().getSimpleName());
            LoggingPreferences.logDetail(log, "Visibility wait stacktrace", e);
            throw e;
        }
    }

    public boolean waitForElementText(By locator, String expectedText) {
        try {
            return new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.textToBe(locator, expectedText));
        } catch (TimeoutException e) {
            log.warn("Timeout waiting for text '{}' on element: {}", expectedText, locator);
            return false;
        }
    }

    public boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isElementVisible(By locator) {
        try {
            new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            log.warn("Element not visible: {}", locator);
            return false;
        }
    }

    public String getElementText(By locator) {
        try {
            WebElement element = waitToBeVisible(locator);
            return element.getText();
        } catch (Exception e) {
            log.error("Error getting element text: {}", locator, e);
            return null;
        }
    }

    @SneakyThrows
    public void click(By locator) {
        StepTracker.record("Click " + locator);
        try {
            WebElement element = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            waitForPostActionSettle();
            log.info("Click performed on: {}", locator);
        } catch (TimeoutException e) {
            log.error("Timeout clicking element: {}", locator);
            LoggingPreferences.logDetail(log, "Click stacktrace", e);
            throw e;
        } catch (Exception e) {
            log.error("Error clicking element: {} ({})", locator, e.getClass().getSimpleName());
            LoggingPreferences.logDetail(log, "Click stacktrace", e);
            throw e;
        }
    }

    public List<WebElement> waitAndGetAll(By locator) {
        new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, 0));
        return driver.findElements(locator);
    }

    public int count(By locator) {
        return waitAndGetAll(locator).size();
    }

    public void clickByIndexInList(By locator, int index) {
        StepTracker.record("Click index " + index + " from " + locator);
        List<WebElement> elements = waitAndGetAll(locator);
        if (index < 0 || index >= elements.size()) {
            throw new IllegalArgumentException(
                    "Index out of range: " + index + " (size=" + elements.size() + ")"
            );
        }
        WebElement target = elements.get(index);
        clickWithFallback(target, locator, index);
    }

    public void clickRandomInList(By locator) {
        List<WebElement> elements = waitAndGetAll(locator);
        int size = elements.size();
        if (size == 0) {
            throw new NoSuchElementException("No elements found for: " + locator);
        }
        int index = ThreadLocalRandom.current().nextInt(size);
        StepTracker.record("Click random index " + index + " from " + locator);
        WebElement target = elements.get(index);
        clickWithFallback(target, locator, index);
    }

    private void clickWithFallback(WebElement element, By locator, int index) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'})", element);
            new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            waitForPostActionSettle();
            log.info("Click by index {} in list: {}", index, locator);
        } catch (Exception e) {
            log.warn("Standard click failed, trying JS click. Reason: {}", e.getMessage());
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            waitForPostActionSettle();
            log.info("JS click performed for index {} on: {}", index, locator);
        }
    }

    public void waitToBePresent(By locator) {
        log.info("<<< WAITING FOR ELEMENT TO BE PRESENT: {} >>>", locator);
        WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);

        if (wait.until(ExpectedConditions.presenceOfElementLocated(locator)) != null) {
            log.info("<<< ELEMENT IS PRESENT: {} >>>", locator);
        } else {
            log.warn("<<< ELEMENT IS NOT PRESENT: {} >>>", locator);
        }
    }

    public boolean clickIfVisible(By locator) {
        if (isElementVisible(locator)) {
            click(locator);
            return true;
        }
        return false;
    }

    public void type(By locator, String text) {
        StepTracker.record("Type into " + locator);
        try {
            WebElement element = waitToBeVisible(locator);
            element.clear();
            element.sendKeys(text);
            log.info("Text sent to: {}", locator);
        } catch (Exception e) {
            log.error("Error sending text to element: {} ({})", locator, e.getClass().getSimpleName());
            LoggingPreferences.logDetail(log, "Type stacktrace", e);
            throw e;
        }
    }

    public void waitLoadingScreen(String xpath) {
        StepTracker.record("Wait loading screen " + xpath);
        By loadingIndicator = By.xpath(xpath);
        try {
            log.info("Waiting for loading indicator visibility...");
            WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);

            wait.until(ExpectedConditions.visibilityOfElementLocated(loadingIndicator));
            log.info("Loading screen visible");

            wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingIndicator));
            log.info("Loading screen disappeared");
        } catch (TimeoutException e) {
            log.error("Loading screen did not disappear after 2 minutes");
            LoggingPreferences.logDetail(log, "Loading wait stacktrace", e);
            throw e;
        }
    }

    public void scrollUntilFoundAndClick(By locator, int maxScrolls, int stepPx) {
        StepTracker.record("Scroll until found and click " + locator);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        for (int i = 0; i < maxScrolls; i++) {
            try {
                WebElement el = driver.findElement(locator);
                wait.until(ExpectedConditions.visibilityOf(el));
                js.executeScript("arguments[0].scrollIntoView({block:'center', behavior:'instant'});", el);
                Thread.sleep(300);
                el.click();
                waitForPostActionSettle();
                log.info("Click performed on {} after {} scrolls", locator, i);
                return;

            } catch (NoSuchElementException e) {
                js.executeScript("window.scrollBy(0, arguments[0]);", stepPx);
                log.debug("Scroll {} (+{} px)", i + 1, stepPx);
                try {
                    Thread.sleep(400);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ignored);
                }
            } catch (TimeoutException e) {
                log.debug("Element found but not visible yet (scroll {}), retrying...", i + 1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        throw new NoSuchElementException("Element not found after " + maxScrolls + " scrolls: " + locator);
    }

    public String takeScreenshotFast() {
        return takeScreenshot(false);
    }

    public String takeScreenshot() {
        return takeScreenshot(true);
    }

    public String takeScreenshot(boolean settleBeforeCapture) {
        StepTracker.record(settleBeforeCapture ? "Take screenshot" : "Take fast screenshot");
        if (driver == null) {
            log.warn("Driver is null. Cannot take screenshot.");
            return null;
        }

        try {
            if (settleBeforeCapture) {
                waitForScreenshotMoment();
            }

            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            String filename = "screenshot_" + Instant.now().toEpochMilli() + ".png";
            Path filePath = resolveScreenshotBaseDir().resolve(filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, screenshot);

            String fullPath = filePath.toAbsolutePath().toString();
            log.info("Screenshot saved at: {}", fullPath);
            return fullPath;

        } catch (IOException e) {
            log.error("Error saving screenshot ({})", e.getClass().getSimpleName());
            LoggingPreferences.logDetail(log, "Screenshot stacktrace", e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error taking screenshot ({})", e.getClass().getSimpleName());
            LoggingPreferences.logDetail(log, "Screenshot stacktrace", e);
            return null;
        }
    }

    private void waitForPostActionSettle() {
        waitForDocumentQuietly(POST_CLICK_SETTLE_TIMEOUT);
    }

    private void waitForScreenshotMoment() {
        waitForDocumentQuietly(SCREENSHOT_SETTLE_TIMEOUT);
    }

    private void waitForDocumentQuietly(Duration timeout) {
        if (!(driver instanceof JavascriptExecutor)) {
            return;
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;

        long deadline = System.nanoTime() + timeout.toNanos();
        String previousSnapshot = readDomSnapshot(js);

        while (System.nanoTime() < deadline) {
            sleep(SCREENSHOT_SETTLE_POLL);

            if (!isDocumentReady(js)) {
                previousSnapshot = readDomSnapshot(js);
                continue;
            }

            String currentSnapshot = readDomSnapshot(js);
            if (currentSnapshot.equals(previousSnapshot)) {
                return;
            }
            previousSnapshot = currentSnapshot;
        }
    }

    private boolean isDocumentReady(JavascriptExecutor js) {
        try {
            Object state = js.executeScript("return document.readyState");
            return "complete".equals(state) || "interactive".equals(state);
        } catch (Exception e) {
            return true;
        }
    }

    private String readDomSnapshot(JavascriptExecutor js) {
        try {
            Object snapshot = js.executeScript(
                    "return [document.readyState,"
                            + "(document.body ? document.body.childElementCount : 0),"
                            + "(document.body ? document.body.innerText.length : 0),"
                            + "window.scrollX, window.scrollY].join('|');"
            );
            return String.valueOf(snapshot);
        } catch (Exception e) {
            return "";
        }
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Path resolveScreenshotBaseDir() {
        String baseDir = System.getenv("DEVICEFARM_SCREENSHOT_PATH");
        if (baseDir == null || baseDir.isBlank()) {
            baseDir = System.getProperty("java.io.tmpdir");
        }
        return Path.of(baseDir);
    }
}
