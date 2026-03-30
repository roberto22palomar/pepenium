package io.github.roberto22palomar.pepenium.toolkit.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import io.github.roberto22palomar.pepenium.toolkit.support.ActionLoggingSupport;
import io.github.roberto22palomar.pepenium.toolkit.support.FastUiSettle;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "The Appium driver is a shared mutable runtime handle intentionally exposed to advanced callers."
)
public class ActionsAppIOS {

    private final AppiumDriver driver;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration LONG_TIMEOUT = Duration.ofSeconds(25);
    private static final Duration POLLING = Duration.ofMillis(60);
    private static final Duration SCREENSHOT_SETTLE_TIMEOUT = Duration.ofMillis(700);
    private static final Duration SCREENSHOT_SETTLE_POLL = Duration.ofMillis(120);
    private static final Duration SCREENSHOT_SETTLE_BUFFER = Duration.ofMillis(80);

    public AppiumDriver getDriver() {
        return this.driver;
    }

    private WebDriverWait shortWait() {
        WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        wait.pollingEvery(POLLING);
        wait.ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        return wait;
    }

    private WebDriverWait longWait() {
        WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);
        wait.pollingEvery(POLLING);
        wait.ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        return wait;
    }

    private <T> T untilShort(Function<? super WebDriver, T> condition) {
        return shortWait().until(condition);
    }

    private <T> T untilLong(Function<? super WebDriver, T> condition) {
        return longWait().until(condition);
    }

    public WebElement waitToBeVisible(By locator) {
        ActionLoggingSupport.recordWait("Wait for visible " + locator);
        return untilLong(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitToBePresent(By locator) {
        ActionLoggingSupport.recordWait("Wait for present " + locator);
        return untilLong(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public WebElement waitToBeClickable(By locator) {
        ActionLoggingSupport.recordWait("Wait for clickable " + locator);
        return untilLong(ExpectedConditions.elementToBeClickable(locator));
    }

    public boolean isElementVisible(By locator) {
        try {
            untilShort(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            log.debug("Not visible within timeout: {}", locator);
            return false;
        }
    }

    public boolean waitForElementText(By locator, String expectedText) {
        try {
            return untilShort(ExpectedConditions.textToBePresentInElementLocated(locator, expectedText));
        } catch (TimeoutException e) {
            log.warn("Timeout waiting for text '{}' on: {}", expectedText, locator);
            return false;
        }
    }

    public boolean waitGone(By locator) {
        try {
            return untilLong(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            log.warn("Timeout waiting for element to disappear: {}", locator);
            return false;
        }
    }

    public String getElementText(By locator) {
        try {
            WebElement el = waitToBeVisible(locator);
            return el.getText();
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "read text", locator, e);
            return null;
        }
    }

    @SneakyThrows
    public void click(By locator) {
        StepTracker.record("Tap " + locator);
        ActionLoggingSupport.recordAction("Tap " + locator);
        try {
            WebElement el = waitToBeClickable(locator);
            el.click();
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "tap", locator, e);
            throw e;
        } catch (ElementClickInterceptedException e) {
            log.warn("Click intercepted, retrying with W3C tap: {}", locator);
            WebElement el = waitToBeVisible(locator);
            Rectangle r = el.getRect();
            int cx = r.getX() + r.getWidth() / 2;
            int cy = r.getY() + r.getHeight() / 2;
            tapPoint(cx, cy, 80);
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "tap", locator, e);
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
        ActionLoggingSupport.recordAction("Type into " + locator);
        try {
            waitStableScreen();
            WebElement el = waitToBeVisible(locator);
            el.clear();
            el.sendKeys(text);
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "type", locator, e);
            throw e;
        }
    }

    public void waitUntilHidden(By loadingLocator) {
        StepTracker.record("Wait until hidden " + loadingLocator);
        ActionLoggingSupport.recordWait("Wait until hidden " + loadingLocator);
        try {
            untilLong(ExpectedConditions.visibilityOfElementLocated(loadingLocator));
        } catch (TimeoutException e) {
            log.warn("Loader did not appear (may be OK): {}", loadingLocator);
        }
        waitGone(loadingLocator);
    }

    public boolean waitStableScreen() {
        final By root = AppiumBy.iOSClassChain("**/XCUIElementTypeWindow[1]");
        final By spinner = AppiumBy.iOSNsPredicateString(
                "type == 'XCUIElementTypeActivityIndicator' AND visible == 1");

        long end = System.nanoTime() + Duration.ofSeconds(3).toNanos();
        Rectangle previous = null;
        int stableCount = 0;

        while (System.nanoTime() < end) {
            try {
                if (!driver.findElements(spinner).isEmpty()) {
                    stableCount = 0;
                    Thread.sleep(200);
                    continue;
                }

                Rectangle current = driver.findElement(root).getRect();
                if (current.equals(previous)) {
                    stableCount++;
                    if (stableCount >= 3) {
                        return true;
                    }
                } else {
                    stableCount = 0;
                }
                previous = current;
                Thread.sleep(250);
            } catch (StaleElementReferenceException e) {
                stableCount = 0;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception any) {
                Dimension size = driver.manage().window().getSize();
                Rectangle fallback = new Rectangle(new Point(0, 0), size);
                if (fallback.equals(previous)) {
                    stableCount++;
                    if (stableCount >= 3) {
                        return true;
                    }
                } else {
                    stableCount = 0;
                }
                previous = fallback;
            }
        }
        return stableCount >= 1;
    }

    public void swipeUp() {
        StepTracker.record("Swipe up");
        ActionLoggingSupport.recordAction("Swipe up");
        waitStableScreen();
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.85);
        int endY = (int) (size.getHeight() * 0.20);
        performSwipe(new Point(x, startY), new Point(x, endY), 500);
    }

    public void swipeDown() {
        StepTracker.record("Swipe down");
        ActionLoggingSupport.recordAction("Swipe down");
        waitStableScreen();
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.20);
        int endY = (int) (size.getHeight() * 0.85);
        performSwipe(new Point(x, startY), new Point(x, endY), 500);
        takeScreenshot();
    }

    public void swipeLeft() {
        StepTracker.record("Swipe left");
        ActionLoggingSupport.recordAction("Swipe left");
        waitStableScreen();
        Dimension size = driver.manage().window().getSize();
        int y = size.getHeight() / 2;
        int startX = (int) (size.getWidth() * 0.85);
        int endX = (int) (size.getWidth() * 0.15);
        performSwipe(new Point(startX, y), new Point(endX, y), 500);
    }

    public void swipeRight() {
        StepTracker.record("Swipe right");
        ActionLoggingSupport.recordAction("Swipe right");
        waitStableScreen();
        Dimension size = driver.manage().window().getSize();
        int y = size.getHeight() / 2;
        int startX = (int) (size.getWidth() * 0.15);
        int endX = (int) (size.getWidth() * 0.85);
        performSwipe(new Point(startX, y), new Point(endX, y), 500);
        takeScreenshot();
    }

    public WebElement scrollToElement(By locator, int maxSwipes) {
        StepTracker.record("Scroll to " + locator);
        ActionLoggingSupport.recordAction("Scroll to " + locator);
        int attempts = 0;
        while (attempts < maxSwipes) {
            try {
                WebElement el = driver.findElement(locator);
                if (el.isDisplayed()) {
                    return el;
                }
            } catch (Exception ignored) {
            }
            swipeUp();
            attempts++;
        }
        throw new NoSuchElementException("Element not found after " + maxSwipes + " swipes: " + locator);
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
            ActionLoggingSupport.recordSavedScreenshot(fullPath);
            return fullPath;
        } catch (IOException e) {
            ActionLoggingSupport.logFailure(log, "save screenshot", "driver capture", e);
            return null;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "take screenshot", "driver capture", e);
            return null;
        }
    }

    public void tapCenter() {
        StepTracker.record("Tap screen center");
        ActionLoggingSupport.recordAction("Tap screen center");
        Dimension size = driver.manage().window().getSize();
        tapPoint(size.width / 2, size.height / 2, 80);
    }

    private void tapPoint(int x, int y, int ms) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, Duration.ofMillis(ms)))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(tap));
    }

    public void swipeAtElement(By locator,
                               Direction direction,
                               int times,
                               double percent,
                               int durationMs) {
        StepTracker.record("Swipe " + direction + " on " + locator + " x" + times);
        ActionLoggingSupport.recordAction("Swipe " + direction + " on " + locator + " x" + times);
        waitStableScreen();
        WebElement el = waitToBeVisible(locator);
        Rectangle r = el.getRect();

        int cx = r.getX() + r.getWidth() / 2;
        int cy = r.getY() + r.getHeight() / 2;

        int dy = (int) Math.max(1, r.getHeight() * percent);
        int dx = (int) Math.max(1, r.getWidth() * percent);

        for (int i = 0; i < times; i++) {
            int startX = cx;
            int startY = cy;
            int endX = cx;
            int endY = cy;
            switch (direction) {
                case UP:
                    endY = cy - dy;
                    break;
                case DOWN:
                    endY = cy + dy;
                    break;
                case LEFT:
                    endX = cx - dx;
                    break;
                case RIGHT:
                    endX = cx + dx;
                    break;
            }
            Point start = clampToViewport(startX, startY);
            Point end = clampToViewport(endX, endY);
            performSwipe(start, end, durationMs);
        }
    }

    private void performSwipe(Point start, Point end, int durationMs) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), start.getX(), start.getY()))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(durationMs),
                        PointerInput.Origin.viewport(), end.getX(), end.getY()))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(swipe));
    }

    public enum Direction { UP, DOWN, LEFT, RIGHT }

    private Point clampToViewport(int x, int y) {
        Dimension size = driver.manage().window().getSize();
        int safeX = Math.max(5, Math.min(size.getWidth() - 5, x));
        int safeY = Math.max(5, Math.min(size.getHeight() - 5, y));
        return new Point(safeX, safeY);
    }

    private void waitForScreenshotMoment() {
        boolean settled = new FastUiSettle(
                driver,
                SCREENSHOT_SETTLE_TIMEOUT,
                SCREENSHOT_SETTLE_POLL,
                SCREENSHOT_SETTLE_BUFFER
        ).waitBriefly();

        if (!settled) {
            log.debug("Screenshot captured without full settle after bounded wait");
        }
    }

    private Path resolveScreenshotBaseDir() {
        return ActionLoggingSupport.resolveScreenshotBaseDir();
    }
}
