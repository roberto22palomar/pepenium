package io.github.roberto22palomar.pepenium.toolkit.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import io.github.roberto22palomar.pepenium.toolkit.support.ActionLoggingSupport;
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
            ActionLoggingSupport.logTimeout(log, "visibility wait", locator, e);
            throw e;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "visibility wait", locator, e);
            throw e;
        }
    }

    public WebElement waitToBePresent(By locator) {
        try {
            return new WebDriverWait(driver, LONG_TIMEOUT)
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "presence wait", locator, e);
            throw e;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "presence wait", locator, e);
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
            ActionLoggingSupport.logFailure(log, "read text", locator, e);
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
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "click", locator, e);
            throw e;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "click", locator, e);
            throw e;
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
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "type", locator, e);
            throw e;
        }
    }

    public void waitUntilHidden(By locator) {
        StepTracker.record("Wait until hidden " + locator);
        try {
            WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "hidden wait", locator, e);
            throw e;
        }
    }

    public String takeScreenshotFast() {
        return takeScreenshot(false);
    }

    public String takeScreenshot() {
        return takeScreenshot(true);
    }

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
            String filename = "screenshot_" + Instant.now().toEpochMilli() + ".png";
            Path filePath = resolveScreenshotBaseDir().resolve(filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, screenshot);

            String fullPath = filePath.toAbsolutePath().toString();
            log.info("Screenshot saved at: {}", fullPath);
            return fullPath;

        } catch (IOException e) {
            ActionLoggingSupport.logFailure(log, "save screenshot", "driver capture", e);
            return null;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "take screenshot", "driver capture", e);
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
}
