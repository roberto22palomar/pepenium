package io.github.roberto22palomar.pepenium.toolkit.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import io.github.roberto22palomar.pepenium.toolkit.support.ActionLoggingSupport;
import io.github.roberto22palomar.pepenium.toolkit.support.ToolkitTimeouts;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
public class ActionsWeb implements WebActions {

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

    @Override
    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
            justification = "ActionsWeb intentionally exposes the active WebDriver for advanced consumer workflows."
    )
    public WebDriver getDriver() {
        return driver;
    }

    @Override
    public void waitForOpenOverlay() {
        new WebDriverWait(driver, defaultTimeout())
                .until(ExpectedConditions.visibilityOfElementLocated(openOverlay));
    }

    @Override
    public void closeSheetIfOpen() {
        List<WebElement> open = driver.findElements(openOverlay);
        if (!open.isEmpty()) {
            StepTracker.record("Close open sheet overlay");
            try {
                click(closeSheetButton);
            } catch (Exception ignore) {
                new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            }
            new WebDriverWait(driver, defaultTimeout())
                    .until(d -> d.findElements(openOverlay).isEmpty());
        }
    }

    @Override
    public void waitForAtLeastNElements(By locator, int n) {
        Objects.requireNonNull(locator, "locator must not be null");
        if (n < 1) {
            throw new IllegalArgumentException("Element count must be at least 1");
        }
        new WebDriverWait(driver, defaultTimeout())
                .until(d -> d.findElements(locator).size() >= n);
    }

    @Override
    public WebElement waitToBeVisible(By locator) {
        ActionLoggingSupport.recordWait("Wait for visible " + locator);
        try {
            return new WebDriverWait(driver, defaultTimeout())
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "visibility wait", locator, e);
            throw e;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "visibility wait", locator, e);
            throw e;
        }
    }

    @Override
    public WebElement waitToBeVisible(WebElement element) {
        ActionLoggingSupport.recordWait("Wait for visible element");
        try {
            return new WebDriverWait(driver, defaultTimeout())
                    .until(ExpectedConditions.visibilityOf(element));
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "visibility wait", element, e);
            throw e;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "visibility wait", element, e);
            throw e;
        }
    }

    @Override
    public WebElement waitToBePresent(By locator) {
        ActionLoggingSupport.recordWait("Wait for present " + locator);
        try {
            return new WebDriverWait(driver, longTimeout())
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "presence wait", locator, e);
            throw e;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "presence wait", locator, e);
            throw e;
        }
    }

    @Override
    public boolean waitForElementText(By locator, String expectedText) {
        Objects.requireNonNull(expectedText, "expectedText must not be null");
        try {
            return new WebDriverWait(driver, defaultTimeout())
                    .until(ExpectedConditions.textToBe(locator, expectedText));
        } catch (TimeoutException e) {
            log.warn("Timeout waiting for text '{}' on element: {}", expectedText, locator);
            return false;
        }
    }

    @Override
    public boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public boolean isElementVisible(By locator) {
        try {
            new WebDriverWait(driver, defaultTimeout())
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            log.warn("Element not visible: {}", locator);
            return false;
        }
    }

    @Override
    public boolean isElementVisible(WebElement element) {
        try {
            new WebDriverWait(driver, defaultTimeout())
                    .until(ExpectedConditions.visibilityOf(element));
            return true;
        } catch (TimeoutException e) {
            log.warn("Element not visible: {}", element);
            return false;
        }
    }

    @Override
    public String getElementText(By locator) {
        try {
            WebElement element = waitToBeVisible(locator);
            return element.getText();
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "read text", locator, e);
            return null;
        }
    }

    @Override
    public String getElementText(WebElement element) {
        try {
            return waitToBeVisible(element).getText();
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "read text", element, e);
            return null;
        }
    }

    @Override
    @SneakyThrows
    public void click(By locator) {
        StepTracker.record("Click " + locator);
        ActionLoggingSupport.recordAction("Click " + locator);
        try {
            WebElement element = new WebDriverWait(driver, defaultTimeout())
                    .until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            waitForPostActionSettle();
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "click", locator, e);
            throw e;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "click", locator, e);
            throw e;
        }
    }

    @Override
    @SneakyThrows
    public void click(WebElement element) {
        StepTracker.record("Click element");
        ActionLoggingSupport.recordAction("Click element");
        try {
            WebElement clickableElement = new WebDriverWait(driver, defaultTimeout())
                    .until(ExpectedConditions.elementToBeClickable(element));
            clickableElement.click();
            waitForPostActionSettle();
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "click", element, e);
            throw e;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "click", element, e);
            throw e;
        }
    }

    @Override
    public boolean clickIfVisible(By locator) {
        if (isElementVisible(locator)) {
            click(locator);
            return true;
        }
        return false;
    }

    @Override
    public boolean clickIfVisible(WebElement element) {
        if (isElementVisible(element)) {
            click(element);
            return true;
        }
        return false;
    }

    @Override
    public void type(By locator, String text) {
        Objects.requireNonNull(text, "text must not be null");
        StepTracker.record("Type into " + locator);
        ActionLoggingSupport.recordAction("Type into " + locator);
        try {
            WebElement element = waitToBeVisible(locator);
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "type", locator, e);
            throw e;
        }
    }

    @Override
    public void type(WebElement element, String text) {
        Objects.requireNonNull(text, "text must not be null");
        StepTracker.record("Type into element");
        ActionLoggingSupport.recordAction("Type into element");
        try {
            WebElement visibleElement = waitToBeVisible(element);
            visibleElement.clear();
            visibleElement.sendKeys(text);
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "type", element, e);
            throw e;
        }
    }

    @Override
    public void waitUntilHidden(By locator) {
        if (!waitGone(locator)) {
            throw new TimeoutException("Timed out waiting for element to be hidden: " + locator);
        }
    }

    @Override
    public boolean waitGone(By locator) {
        StepTracker.record("Wait until hidden " + locator);
        ActionLoggingSupport.recordWait("Wait until hidden " + locator);
        try {
            WebDriverWait wait = new WebDriverWait(driver, longTimeout());
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "hidden wait", locator, e);
            return false;
        }
    }

    @Override
    public String takeScreenshotFast() {
        return takeScreenshot(false);
    }

    @Override
    public String takeScreenshot() {
        return takeScreenshot(true);
    }

    @Override
    @SuppressFBWarnings(
            value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "Screenshot output falls back to a concrete filesystem path before resolution."
    )
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
            String filename = uniqueScreenshotFileName("screenshot");
            Path screenshotBaseDir = resolveScreenshotBaseDir();
            Path filePath = screenshotBaseDir.resolve(filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, screenshot);

            String fullPath = filePath.toAbsolutePath().toString();
            log.info("Screenshot saved at: {}", fullPath);
            ActionLoggingSupport.recordSavedScreenshot(fullPath);
            return fullPath;

        } catch (IOException e) {
            ActionLoggingSupport.logFailure(log, "save screenshot",
                    "driver capture under " + resolveScreenshotBaseDir().toAbsolutePath(), e);
            return null;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "take screenshot",
                    "driver capture under " + resolveScreenshotBaseDir().toAbsolutePath(), e);
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
        return ActionLoggingSupport.resolveScreenshotBaseDir();
    }

    private Duration defaultTimeout() {
        return ToolkitTimeouts.actionTimeout(DEFAULT_TIMEOUT);
    }

    private Duration longTimeout() {
        return ToolkitTimeouts.longActionTimeout(LONG_TIMEOUT);
    }

    private String uniqueScreenshotFileName(String prefix) {
        return prefix + "_" + Instant.now().toEpochMilli()
                + "_" + Long.toUnsignedString(System.nanoTime(), 36) + ".png";
    }
}
