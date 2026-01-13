package io.github.roberto22palomar.pepenium.toolkit.utils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
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
public class ActionsAppIOS {

    private final AppiumDriver driver;

    // Tune these timings for your grid (iOS usually needs more margin than Android)
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration LONG_TIMEOUT    = Duration.ofSeconds(25);
    private static final Duration POLLING         = Duration.ofMillis(60);

    public AppiumDriver getDriver() { return this.driver; }

    // =========================
    // Wait helpers (internal)
    // =========================
    private WebDriverWait shortWait() {
        WebDriverWait w = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        w.pollingEvery(POLLING);
        w.ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        return w;
    }

    private WebDriverWait longWait() {
        WebDriverWait w = new WebDriverWait(driver, LONG_TIMEOUT);
        w.pollingEvery(POLLING);
        w.ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        return w;
    }

    private <T> T untilShort(Function<? super WebDriver, T> condition) {
        return shortWait().until(condition);
    }

    private <T> T untilLong(Function<? super WebDriver, T> condition) {
        return longWait().until(condition);
    }

    // =========================
    // Waits and validations
    // =========================

    /** Visible on screen (not just present in the tree). */
    public WebElement waitToBeVisible(By locator) {
        log.info("<<< WAIT visible: {} >>>", locator);
        return untilLong(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Present (without requiring visibility). Useful for lazy loads or collections. */
    public WebElement waitToBePresent(By locator) {
        log.info("<<< WAIT present: {} >>>", locator);
        return untilLong(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /** Clickable ≈ visible + enabled (not always perfect on iOS, but helps). */
    public WebElement waitToBeClickable(By locator) {
        log.info("<<< WAIT clickable: {} >>>", locator);
        return untilLong(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Returns true if the element becomes visible within the short timeout. */
    public boolean isElementVisible(By locator) {
        try {
            untilShort(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            log.debug("Not visible within timeout: {}", locator);
            return false;
        }
    }

    /** Waits until the visible text contains the expected string. */
    public boolean waitForElementText(By locator, String expectedText) {
        try {
            return untilShort(ExpectedConditions.textToBePresentInElementLocated(locator, expectedText));
        } catch (TimeoutException e) {
            log.warn("Timeout waiting for text '{}' on: {}", expectedText, locator);
            return false;
        }
    }

    /** Waits until the locator disappears (invisible or not present). */
    public boolean waitGone(By locator) {
        try {
            return untilLong(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            log.warn("Timeout waiting for element to disappear: {}", locator);
            return false;
        }
    }

    /** Gets the text from a visible element (re-locates to reduce stale issues). */
    public String getElementText(By locator) {
        try {
            WebElement el = waitToBeVisible(locator);
            return el.getText();
        } catch (Exception e) {
            log.error("Error getting text from element: {}", locator, e);
            return null;
        }
    }

    // =========================
    // Basic interactions
    // =========================
    @SneakyThrows
    public void click(By locator) {
        try {
            WebElement el = waitToBeClickable(locator);
            el.click();
            log.info("CLICK on: {}", locator);
        } catch (TimeoutException e) {
            log.error("Timeout clicking element: {}", locator, e);
            throw e;
        } catch (ElementClickInterceptedException ice) {
            // Soft retry: small W3C tap at element center
            log.warn("Click intercepted, retrying with W3C tap: {}", locator);
            WebElement el = waitToBeVisible(locator);
            Rectangle r = el.getRect();
            int cx = r.getX() + r.getWidth() / 2;
            int cy = r.getY() + r.getHeight() / 2;
            tapPoint(cx, cy, 80);
        } catch (Exception e) {
            log.error("Error clicking element: {}", locator, e);
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
        try {
            waitStableScreen();
            WebElement el = waitToBeVisible(locator);
            el.clear();
            el.sendKeys(text);
            // On iOS the keyboard may cover the next button:
            try { /* driver.hideKeyboard(); */ } catch (Exception ignore) {}
            log.info("Text sent to {}: '{}'", locator, text);
        } catch (Exception e) {
            log.error("Error sending text to element: {}", locator, e);
            throw e;
        }
    }

    /** Waits for a custom loader: waits for it to appear and then disappear. */
    public void waitLoadingScreenToDisappear(By loadingLocator) {
        log.info("<<< WAIT loader visible: {} >>>", loadingLocator);
        try {
            untilLong(ExpectedConditions.visibilityOfElementLocated(loadingLocator));
        } catch (TimeoutException e) {
            log.warn("Loader did not appear (may be OK): {}", loadingLocator);
        }
        log.info("<<< WAIT loader gone: {} >>>", loadingLocator);
        waitGone(loadingLocator);
    }

    // =========================
    // Screen stability (iOS)
    // =========================
    public boolean waitStableScreen() {
        final By ROOT    = AppiumBy.iOSClassChain("**/XCUIElementTypeWindow[1]");
        final By SPINNER = AppiumBy.iOSNsPredicateString(
                "type == 'XCUIElementTypeActivityIndicator' AND visible == 1");

        long end = System.nanoTime() + Duration.ofSeconds(3).toNanos();
        Rectangle previous = null;
        int stableCount = 0;

        while (System.nanoTime() < end) {
            try {
                // If spinner is visible, reset counter
                if (!driver.findElements(SPINNER).isEmpty()) {
                    stableCount = 0;
                    Thread.sleep(200);
                    continue;
                }

                Rectangle current = driver.findElement(ROOT).getRect();
                if (current.equals(previous)) {
                    stableCount++;
                    if (stableCount >= 3) return true; // ~600-800ms stable
                } else {
                    stableCount = 0;
                }
                previous = current;
                Thread.sleep(250);

            } catch (StaleElementReferenceException e) {
                stableCount = 0;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception any) {
                // Fallback to viewport
                Dimension size = driver.manage().window().getSize();
                Rectangle fallback = new Rectangle(new Point(0, 0), size);
                if (fallback.equals(previous)) {
                    stableCount++;
                    if (stableCount >= 3) return true;
                } else {
                    stableCount = 0;
                }
                previous = fallback;
            }
        }
        return stableCount >= 1; // some stability at least
    }

    // =========================
    // Gestures and scrolling
    // =========================
    public void swipeUp() {
        waitStableScreen();
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.85);
        int endY   = (int) (size.getHeight() * 0.20);
        performSwipe(new Point(x, startY), new Point(x, endY), 500);
    }

    public void swipeDown() {
        waitStableScreen();
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.20);
        int endY   = (int) (size.getHeight() * 0.85);
        performSwipe(new Point(x, startY), new Point(x, endY), 500);
        takeScreenshot();
    }

    public void swipeLeft() {
        waitStableScreen();
        Dimension size = driver.manage().window().getSize();
        int y = size.getHeight() / 2;
        int startX = (int) (size.getWidth() * 0.85);
        int endX   = (int) (size.getWidth() * 0.15);
        performSwipe(new Point(startX, y), new Point(endX, y), 500);
    }

    public void swipeRight() {
        waitStableScreen();
        Dimension size = driver.manage().window().getSize();
        int y = size.getHeight() / 2;
        int startX = (int) (size.getWidth() * 0.15);
        int endX   = (int) (size.getWidth() * 0.85);
        performSwipe(new Point(startX, y), new Point(endX, y), 500);
        takeScreenshot();
    }

    /** Repeated swipe scrolling until the element is visible or attempts are exhausted. */
    public WebElement scrollToElement(By locator, int maxSwipes) {
        int attempts = 0;
        while (attempts < maxSwipes) {
            try {
                WebElement el = driver.findElement(locator);
                if (el.isDisplayed()) return el;
            } catch (Exception ignored) { }
            swipeUp();
            attempts++;
        }
        throw new NoSuchElementException("Element not found after " + maxSwipes + " swipes: " + locator);
    }

    // =========================
    // Screenshots / Debug
    // =========================
    public String takeScreenshot() {
        waitStableScreen();
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            String filename = "screenshot_" + Instant.now().toEpochMilli() + ".png";
            Path filePath = Path.of("/tmp", filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, screenshot);
            String fullPath = filePath.toAbsolutePath().toString();
            log.info("📸 Screenshot saved at: {}", fullPath);
            return fullPath;
        } catch (IOException e) {
            log.error("Error saving screenshot", e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error taking screenshot", e);
            return null;
        }
    }

    public void tapCenter() {
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

    public void safeScrollToElement(By locator) {
        ScrollUtils scroller = new ScrollUtils(driver);
        scroller.scrollToElement(locator, 12);
    }

    // === SWIPE IN ELEMENT AREA ===
    public void swipeAtElement(By locator,
                               Direction direction,
                               int times,
                               double percent,
                               int durationMs) {
        waitStableScreen();
        WebElement el = waitToBeVisible(locator);
        Rectangle r = el.getRect();

        int cx = r.getX() + r.getWidth() / 2;
        int cy = r.getY() + r.getHeight() / 2;

        int dy = (int) Math.max(1, r.getHeight() * percent);
        int dx = (int) Math.max(1, r.getWidth()  * percent);

        for (int i = 0; i < times; i++) {
            int startX = cx, startY = cy, endX = cx, endY = cy;
            switch (direction) {
                case UP:    endY = cy - dy; break;
                case DOWN:  endY = cy + dy; break;
                case LEFT:  endX = cx - dx; break;
                case RIGHT: endX = cx + dx; break;
            }
            Point start = clampToViewport(startX, startY);
            Point end   = clampToViewport(endX, endY);
            performSwipe(start, end, durationMs);
            log.info("Swipe {} on {} ({} -> {})", direction, locator, start, end);
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

    private void performSwipeIOS(WebElement el, Point start, Point end, int durationMs) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

        // Ensure minimum duration so iOS detects the gesture
        int safeDuration = Math.max(durationMs, 400);

        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), start.getX(), start.getY()))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(safeDuration),
                        PointerInput.Origin.viewport(), end.getX(), end.getY()))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }

    private Point clampToViewportIOS(int x, int y) {
        Dimension size = driver.manage().window().getSize();

        // Larger margin on iOS to avoid system gestures
        int margin = 20;

        int safeX = Math.max(margin, Math.min(size.getWidth()  - margin, x));
        int safeY = Math.max(margin, Math.min(size.getHeight() - margin, y));

        return new Point(safeX, safeY);
    }

    public enum Direction { UP, DOWN, LEFT, RIGHT }

    /**
     * Performs a swipe on an element in iOS (XCUITest driver).
     */
    public void swipeAtElementIOS(By locator,
                                  Direction direction,
                                  int times,
                                  double percent,
                                  int durationMs) {

        waitStableScreen();
        WebElement el = waitToBeVisible(locator);
        Rectangle r = el.getRect();

        // Element center
        int cx = r.getX() + r.getWidth() / 2;
        int cy = r.getY() + r.getHeight() / 2;

        // Swipe distance (safer for iOS)
        int dy = (int) Math.max(10, r.getHeight() * percent);
        int dx = (int) Math.max(10, r.getWidth()  * percent);

        for (int i = 0; i < times; i++) {
            int startX = cx, startY = cy, endX = cx, endY = cy;

            switch (direction) {
                case UP:    endY = cy - dy; break;
                case DOWN:  endY = cy + dy; break;
                case LEFT:  endX = cx - dx; break;
                case RIGHT: endX = cx + dx; break;
            }

            Point start = clampToViewportIOS(startX, startY);
            Point end   = clampToViewportIOS(endX, endY);

            performSwipeIOS(el, start, end, durationMs);
            log.info("📱 Swipe {} (iOS) on {} -> {} → {}", direction, locator, start, end);
        }
    }

    private Point clampToViewport(int x, int y) {
        Dimension size = driver.manage().window().getSize();
        int safeX = Math.max(5, Math.min(size.getWidth()  - 5, x));
        int safeY = Math.max(5, Math.min(size.getHeight() - 5, y));
        return new Point(safeX, safeY);
    }
}
