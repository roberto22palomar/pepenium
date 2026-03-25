package io.github.roberto22palomar.pepenium.toolkit.actions;

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
public class ActionsApp {

    private final AppiumDriver driver;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(6L);
    private static final Duration LONG_TIMEOUT = Duration.ofSeconds(120L);
    private static final Duration SCREENSHOT_SETTLE_TIMEOUT = Duration.ofMillis(700);
    private static final Duration SCREENSHOT_SETTLE_POLL = Duration.ofMillis(120);
    private static final Duration SCREENSHOT_SETTLE_BUFFER = Duration.ofMillis(80);

    public WebElement waitToBePresent(By locator) {
        WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
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
            WebElement element = waitToBePresent(locator);
            return element.getText();
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "read text", locator, e);
            return null;
        }
    }

    @SneakyThrows
    public void makeClick(By locator) {
        StepTracker.record("Tap " + locator);
        try {
            WebElement element = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "tap", locator, e);
            throw e;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "tap", locator, e);
            throw e;
        }
    }

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

    public boolean makeClickIfVisible(By locator) throws InterruptedException {
        if (isElementVisible(locator)) {
            makeClick(locator);
            return true;
        }
        return false;
    }

    public void sendText(By locator, String text) {
        StepTracker.record("Type into " + locator);
        try {
            waitStableScreen();
            WebElement element = waitToBePresent(locator);
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "type", locator, e);
            throw e;
        }
    }

    public void waitLoadingScreenToDisappear(String xpath) {
        StepTracker.record("Wait loading screen " + xpath);
        By loadingIndicator = By.xpath(xpath);
        try {
            WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);
            wait.until(ExpectedConditions.visibilityOfElementLocated(loadingIndicator));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingIndicator));
        } catch (TimeoutException e) {
            ActionLoggingSupport.logTimeout(log, "loading wait", loadingIndicator, e);
            throw e;
        }
    }

    public void swipeUp() {
        StepTracker.record("Swipe up");
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

    public void swipeDown() {
        StepTracker.record("Swipe down");
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

    public void swipeLeft() {
        StepTracker.record("Swipe left");
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

    public void swipeRight() {
        StepTracker.record("Swipe right");
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

    public WebElement scrollToElement(By locator, int maxSwipes) {
        StepTracker.record("Scroll to " + locator);
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
        StepTracker.record("Swipe " + direction + " on " + locator + " x" + times);

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

    private Point clampToViewport(int x, int y) {
        Dimension size = driver.manage().window().getSize();
        int safeX = Math.max(5, Math.min(size.getWidth() - 5, x));
        int safeY = Math.max(5, Math.min(size.getHeight() - 5, y));
        return new Point(safeX, safeY);
    }

    public enum Direction {UP, DOWN, LEFT, RIGHT}

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
            ActionLoggingSupport.logFailure(log, "save screenshot", "driver capture", e);
            return null;
        } catch (Exception e) {
            ActionLoggingSupport.logFailure(log, "take screenshot", "driver capture", e);
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
}
