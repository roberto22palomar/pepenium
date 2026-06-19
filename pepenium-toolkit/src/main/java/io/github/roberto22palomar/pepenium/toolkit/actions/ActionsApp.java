package io.github.roberto22palomar.pepenium.toolkit.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import io.github.roberto22palomar.pepenium.toolkit.support.ActionLoggingSupport;
import io.github.roberto22palomar.pepenium.toolkit.support.FastUiSettle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
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

@RequiredArgsConstructor
@Slf4j
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "The Appium driver is a shared mutable runtime handle intentionally exposed to advanced callers."
)
public class ActionsApp implements MobileActions {

    private final AppiumDriver driver;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(6L);
    private static final Duration LONG_TIMEOUT = Duration.ofSeconds(120L);
    private static final Duration SCREENSHOT_SETTLE_TIMEOUT = Duration.ofMillis(700);
    private static final Duration SCREENSHOT_SETTLE_POLL = Duration.ofMillis(120);
    private static final Duration SCREENSHOT_SETTLE_BUFFER = Duration.ofMillis(80);

    @Override
    public AppiumDriver getDriver() {
        return this.driver;
    }

    @Override
    public WebElement waitToBePresent(By locator) {
        ActionLoggingSupport.recordWait("Wait for present " + locator);
        WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    @Override
    public WebElement waitToBeVisible(By locator) {
        ActionLoggingSupport.recordWait("Wait for visible " + locator);
        WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    @Override
    public WebElement waitToBeClickable(By locator) {
        ActionLoggingSupport.recordWait("Wait for clickable " + locator);
        WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    @Override
    public boolean waitForElementText(By locator, String expectedText) {
        try {
            return new WebDriverWait(driver, DEFAULT_TIMEOUT)
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
            new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            log.warn("Element not visible: {}", locator);
            return false;
        }
    }

    @Override
    public boolean waitGone(By locator) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            log.warn("Timeout waiting for element to disappear: {}", locator);
            return false;
        }
    }

    @Override
    public String getElementText(By locator) {
        try {
            WebElement element = waitToBePresent(locator);
            return element.getText();
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "read text", locator, e);
            return null;
        }
    }

    @Override
    public void click(By locator) {
        StepTracker.record("Tap " + locator);
        ActionLoggingSupport.recordAction("Tap " + locator);
        try {
            WebElement element = waitToBeClickable(locator);
            element.click();
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "tap", locator, e);
            throw e;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "tap", locator, e);
            throw e;
        }
    }

    @Override
    public boolean waitStableScreen() {
        final By root = AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"android:id/content\")");
        final By spinner = AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.ProgressBar\")");

        long end = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        Rectangle previous = null;
        int consecutiveStable = 0;

        while (System.nanoTime() < end) {
            try {
                if (!driver.findElements(spinner).isEmpty()) {
                    consecutiveStable = 0;
                    Thread.sleep(200);
                    continue;
                }

                Rectangle current = driver.findElement(root).getRect();
                if (current.equals(previous)) {
                    if (++consecutiveStable >= 2) {
                        return true;
                    }
                } else {
                    consecutiveStable = 0;
                }

                previous = current;
                Thread.sleep(200);
            } catch (StaleElementReferenceException e) {
                consecutiveStable = 0;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
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
    public void type(By locator, String text) {
        StepTracker.record("Type into " + locator);
        ActionLoggingSupport.recordAction("Type into " + locator);
        try {
            waitStableScreen();
            WebElement element = waitToBeVisible(locator);
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "type", locator, e);
            throw e;
        }
    }

    @Override
    public void waitUntilHidden(By locator) {
        StepTracker.record("Wait until hidden " + locator);
        ActionLoggingSupport.recordWait("Wait until hidden " + locator);
        if (!waitGone(locator)) {
            TimeoutException e = new TimeoutException("Element stayed visible: " + locator);
            ActionLoggingSupport.logTimeout(log, "hidden wait", locator, e);
            throw e;
        }
    }

    @Override
    public void swipeUp() {
        StepTracker.record("Swipe up");
        ActionLoggingSupport.recordAction("Swipe up");
        waitStableScreen();
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.90);
        int endY = (int) (size.getHeight() * 0.50);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), x, startY))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(500),
                        PointerInput.Origin.viewport(), x, endY))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }

    @Override
    public void swipeDown() {
        StepTracker.record("Swipe down");
        ActionLoggingSupport.recordAction("Swipe down");
        waitStableScreen();
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.10);
        int endY = (int) (size.getHeight() * 0.90);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), x, startY))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(500),
                        PointerInput.Origin.viewport(), x, endY))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
        takeScreenshot();
    }

    @Override
    public void swipeLeft() {
        StepTracker.record("Swipe left");
        ActionLoggingSupport.recordAction("Swipe left");
        waitStableScreen();
        Dimension size = driver.manage().window().getSize();
        int y = size.getHeight() / 2;
        int startX = (int) (size.getWidth() * 0.90);
        int endX = (int) (size.getWidth() * 0.10);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), startX, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(500),
                        PointerInput.Origin.viewport(), endX, y))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }

    @Override
    public void swipeRight() {
        StepTracker.record("Swipe right");
        ActionLoggingSupport.recordAction("Swipe right");
        waitStableScreen();
        Dimension size = driver.manage().window().getSize();
        int y = size.getHeight() / 2;
        int startX = (int) (size.getWidth() * 0.10);
        int endX = (int) (size.getWidth() * 0.90);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), startX, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(500),
                        PointerInput.Origin.viewport(), endX, y))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
        takeScreenshot();
    }

    @Override
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
                log.debug("Element not ready while scrolling to {}", locator);
            }

            swipeUp();
            attempts++;
        }
        throw new NoSuchElementException("Element not found after " + maxSwipes + " swipes: " + locator);
    }

    public void swipeAtElement(By locator,
                               Direction direction,
                               int times,
                               double percent,
                               int durationMs) {
        validateSwipeAtElementArguments(direction, times, percent, durationMs);
        StepTracker.record("Swipe " + direction + " on " + locator + " x" + times);
        ActionLoggingSupport.recordAction("Swipe " + direction + " on " + locator + " x" + times);

        waitStableScreen();
        WebElement el = waitToBePresent(locator);
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

    private void validateSwipeAtElementArguments(Direction direction, int times, double percent, int durationMs) {
        if (direction == null) {
            throw new IllegalArgumentException("Swipe direction must not be null");
        }
        if (times < 1) {
            throw new IllegalArgumentException("Swipe times must be at least 1");
        }
        if (percent <= 0.0 || percent > 1.0) {
            throw new IllegalArgumentException("Swipe percent must be greater than 0 and at most 1");
        }
        if (durationMs < 1) {
            throw new IllegalArgumentException("Swipe durationMs must be at least 1");
        }
    }

    private void validateSwipeAtElementArguments(SwipeDirection direction, int times, double percent, int durationMs) {
        if (direction == null) {
            throw new IllegalArgumentException("Swipe direction must not be null");
        }
        validateSwipeAtElementArguments(Direction.valueOf(direction.name()), times, percent, durationMs);
    }

    @Override
    public void swipeAtElement(By locator,
                               SwipeDirection direction,
                               int times,
                               double percent,
                               int durationMs) {
        validateSwipeAtElementArguments(direction, times, percent, durationMs);
        swipeAtElement(locator, Direction.valueOf(direction.name()), times, percent, durationMs);
    }

    @Override
    public void tapCenter() {
        StepTracker.record("Tap screen center");
        ActionLoggingSupport.recordAction("Tap screen center");
        Dimension size = driver.manage().window().getSize();
        tapPoint(size.width / 2, size.height / 2, 80);
    }

    private void tapPoint(int x, int y, int ms) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(ms),
                        PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(tap));
    }

    private Point clampToViewport(int x, int y) {
        Dimension size = driver.manage().window().getSize();
        int safeX = Math.max(5, Math.min(size.getWidth() - 5, x));
        int safeY = Math.max(5, Math.min(size.getHeight() - 5, y));
        return new Point(safeX, safeY);
    }

    public enum Direction {UP, DOWN, LEFT, RIGHT}

    @Override
    public String takeScreenshotFast() {
        return takeScreenshot(false);
    }

    @Override
    public String takeScreenshot() {
        return takeScreenshot(true);
    }

    @SuppressFBWarnings(
            value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "Screenshot output falls back to a concrete filesystem path before resolution."
    )
    @Override
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

    private String uniqueScreenshotFileName(String prefix) {
        return prefix + "_" + Instant.now().toEpochMilli()
                + "_" + Long.toUnsignedString(System.nanoTime(), 36) + ".png";
    }
}
