package io.github.roberto22palomar.pepenium.toolkit.utils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
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

    // ---------------------------
    // Block: Waits and validations
    // ---------------------------

    /**
     * Waits until the element is present on screen.
     */
    public WebElement waitToBePresent(By locator) {
        log.info("<<< WAITING FOR ELEMENT TO BE PRESENT: {}", locator);
        WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

        if (element != null) {
            log.info("<<< ELEMENT IS PRESENT: {}", locator);
        } else {
            log.warn("<<< ELEMENT IS NOT PRESENT: {}", locator);
        }
        return element;
    }

    /**
     * Waits until the element text matches the expected value.
     */
    public boolean waitForElementText(By locator, String expectedText) {
        try {
            return new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.textToBe(locator, expectedText));
        } catch (TimeoutException e) {
            log.warn("Timeout waiting for text '{}' on element: {}", expectedText, locator);
            return false;
        }
    }

    /**
     * Checks whether an element is present without waiting.
     */
    public boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Checks whether an element is visible.
     */
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

    /**
     * Gets the visible text from an element.
     */
    public String getElementText(By locator) {
        try {
            WebElement element = waitToBePresent(locator);
            return element.getText();
        } catch (Exception e) {
            log.error("Error getting text from element: {}", locator, e);
            return null;
        }
    }

    // ---------------------------
    // Block: Basic interactions
    // ---------------------------

    /**
     * Waits until the element is clickable and clicks it.
     */
    @SneakyThrows
    public void makeClick(By locator) {
        try {
            WebElement element = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            log.info("Click performed on: {}", locator);
        } catch (TimeoutException e) {
            log.error("Timeout clicking element: {}", locator, e);
            throw e;
        } catch (Exception e) {
            log.error("Error clicking element: {}", locator, e);
            throw e;
        }
    }

    /**
     * Tries to detect a "stable" screen state by checking:
     * - no visible spinner/progress bar
     * - root container rectangle unchanged for a few consecutive checks
     */
    public boolean waitStableScreen() {
        final By ROOT = AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"android:id/content\")");
        final By SPINNER = AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.ProgressBar\")");

        long end = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        org.openqa.selenium.Rectangle previous = null;
        int consecutiveStable = 2;

        while (System.nanoTime() < end) {
            try {
                // If a loader is visible, we do not consider the UI stable
                if (!driver.findElements(SPINNER).isEmpty()) {
                    consecutiveStable = 0;
                    Thread.sleep(1500);
                    continue;
                }

                org.openqa.selenium.Rectangle current = driver.findElement(ROOT).getRect();

                if (current.equals(previous)) {
                    if (++consecutiveStable >= 3) return true; // ✔ stable
                } else {
                    consecutiveStable = 0;
                }

                previous = current;
                Thread.sleep(1500);

            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                consecutiveStable = 0; // root was recreated → retry
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false; // did not stabilize in time
    }

    /**
     * Clicks if the element is visible.
     */
    public boolean makeClickIfVisible(By locator) throws InterruptedException {
        if (isElementVisible(locator)) {
            makeClick(locator);
            return true;
        }
        return false;
    }

    /**
     * Waits for the field to be visible, clears it and sends text.
     */
    public void sendText(By locator, String text) {
        try {
            waitStableScreen();
            WebElement element = waitToBePresent(locator);
            element.clear();
            element.sendKeys(text);
            log.info("Text sent to: {}", locator);
        } catch (Exception e) {
            log.error("Error sending text to element: {}", locator, e);
            throw e;
        }
    }

    /**
     * Waits for a loading indicator to appear and then disappear.
     */
    public void waitLoadingScreenToDisappear(String xpath) {
        By loadingIndicator = By.xpath(xpath);
        try {
            log.info("Waiting for loading indicator to become visible...");
            WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);

            wait.until(ExpectedConditions.visibilityOfElementLocated(loadingIndicator));
            log.info("✅ Loading screen visible");

            wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingIndicator));
            log.info("✅ Loading screen disappeared");
        } catch (TimeoutException e) {
            log.error("⚠️ Loading screen did not disappear after 2 minutes", e);
            throw e;
        }
    }

    // ---------------------------
    // Block: Gestures and movements
    // ---------------------------

    /**
     * Swipe up.
     */
    public void swipeUp() {
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

    /**
     * Swipe down.
     */
    public void swipeDown() {
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

    /**
     * Swipe left.
     */
    public void swipeLeft() {
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

    /**
     * Swipe right.
     */
    public void swipeRight() {
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

    /**
     * Repeatedly swipes up until the element is visible or the max number of swipes is reached.
     */
    public WebElement scrollToElement(By locator, int maxSwipes) {
        int attempts = 0;
        while (attempts < maxSwipes) {
            try {
                WebElement el = driver.findElement(locator);
                if (el.isDisplayed()) {
                    return el;
                }
            } catch (Exception ignored) {
                log.error("Error while scrolling to element: {}", ignored.getMessage());
            }

            swipeUp();
            attempts++;
        }
        throw new NoSuchElementException("Element not found after " + maxSwipes + " swipes: " + locator);
    }

    public void swipeAtElement(By locator,
                               Direction direction,
                               int times,
                               double percent,          // 0.0 - 1.0 of the element height/width
                               int durationMs) {        // gesture duration

        waitStableScreen();
        WebElement el = waitToBePresent(locator);
        Rectangle r = el.getRect();

        // Center of the element
        int cx = r.getX() + r.getWidth() / 2;
        int cy = r.getY() + r.getHeight() / 2;

        // Swipe distance based on element size
        int dy = (int) Math.max(1, r.getHeight() * percent);
        int dx = (int) Math.max(1, r.getWidth() * percent);

        for (int i = 0; i < times; i++) {
            int startX = cx, startY = cy, endX = cx, endY = cy;

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
            log.info("Swipe {} in area of {} ({} -> {})", direction, locator, start, end);
        }
    }

    // Executes the swipe using W3C Actions
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

    // Clamps coordinates to the screen bounds
    private Point clampToViewport(int x, int y) {
        Dimension size = driver.manage().window().getSize();
        int safeX = Math.max(5, Math.min(size.getWidth() - 5, x));
        int safeY = Math.max(5, Math.min(size.getHeight() - 5, y));
        return new Point(safeX, safeY);
    }

    // Enum for directions
    public enum Direction {UP, DOWN, LEFT, RIGHT}

    // ---------------------------
    // Block: Screenshots and debugging
    // ---------------------------

    public String takeScreenshot() {
        waitStableScreen();
        if (driver == null) {
            log.warn("⚠️ Driver is null. Cannot take screenshot.");
            return null;
        }

        try {
            // Quick wait for the UI to settle
            new FastUiSettle(driver).waitBriefly();
            // Screenshot as bytes
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            // Base path from env var or /tmp
            // - In AWS DF it can be collected as an artifact if configured
            // - In BrowserStack/local: saved under /tmp (or provided path)
            String baseDir = System.getenv("DEVICEFARM_SCREENSHOT_PATH");
            if (baseDir == null || baseDir.isEmpty()) {
                baseDir = "/tmp";
            }

            // Filename with timestamp
            String filename = "screenshot_" + Instant.now().toEpochMilli() + ".png";

            // Build Path and write file
            Path filePath = Path.of(baseDir, filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, screenshot);

            String fullPath = filePath.toAbsolutePath().toString();
            log.info("📸 Screenshot saved at: {}", fullPath);
            return fullPath;

        } catch (IOException e) {
            log.error("💥 Error saving screenshot", e);
            return null;
        } catch (Exception e) {
            log.error("💥 Unexpected error taking screenshot", e);
            return null;
        }
    }
}
