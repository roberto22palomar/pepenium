package io.github.roberto22palomar.pepenium.toolkit.utils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
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

    // ====== Overlay helpers + robust click ======
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
            // Try closing with a button; if not available, ESC as fallback
            try {
                click(closeSheetButton);
            } catch (Exception ignore) {
                new org.openqa.selenium.interactions.Actions(driver)
                        .sendKeys(Keys.ESCAPE)
                        .perform();
            }
            new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(d -> d.findElements(openOverlay).isEmpty());
        }
    }

    /**
     * Waits until there are at least N elements (useful for dynamic lists like chips).
     */
    public void waitForAtLeastNElements(By locator, int n) {
        new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(d -> d.findElements(locator).size() >= n);
    }

    // ---------------------------
    // Block: Waits and validations
    // ---------------------------

    /**
     * Waits until the element is visible on screen.
     */
    public WebElement waitToBeVisible(By locator) {
        try {
            return new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            log.error("Timeout waiting for element visibility: {}", locator, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while waiting for element visibility: {}", locator, e);
            throw e;
        }
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
            WebElement element = waitToBeVisible(locator);
            return element.getText();
        } catch (Exception e) {
            log.error("Error getting element text: {}", locator, e);
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
    public void click(By locator) {
        try {
            WebElement element = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            Thread.sleep(2000);
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
     * Waits until at least one element exists for the locator and returns them all.
     */
    public List<WebElement> waitAndGetAll(By locator) {
        new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, 0));
        return driver.findElements(locator);
    }

    /**
     * Counts elements for a locator (waiting until at least one exists).
     */
    public int count(By locator) {
        return waitAndGetAll(locator).size();
    }

    /**
     * Click an element by index within a list located by 'locator'.
     */
    public void clickByIndexInList(By locator, int index) {
        List<WebElement> elements = waitAndGetAll(locator);
        if (index < 0 || index >= elements.size()) {
            throw new IllegalArgumentException(
                    "Index out of range: " + index + " (size=" + elements.size() + ")"
            );
        }
        WebElement target = elements.get(index);
        clickWithFallback(target, locator, index);
    }

    /**
     * Click a random element within a list located by 'locator'.
     */
    public void clickRandomInList(By locator) {
        List<WebElement> elements = waitAndGetAll(locator);
        int size = elements.size();
        if (size == 0) {
            throw new NoSuchElementException("No elements found for: " + locator);
        }
        int index = ThreadLocalRandom.current().nextInt(size);
        WebElement target = elements.get(index);
        clickWithFallback(target, locator, index);
    }

    /**
     * Click with scroll and JS fallback (floating menus, overlays, etc).
     */
    private void clickWithFallback(WebElement element, By locator, int index) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'})", element);
            new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            log.info("Click by index {} in list: {}", index, locator);
        } catch (Exception e) {
            log.warn("Standard click failed, trying JS click. Reason: {}", e.getMessage());
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            log.info("JS click performed for index {} on: {}", index, locator);
        }
    }

    /**
     * Waits for element presence (useful for containers like menus).
     */
    public void waitToBePresent(By locator) {
        log.info("<<< WAITING FOR ELEMENT TO BE PRESENT: {} >>>", locator);
        WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);

        if (wait.until(ExpectedConditions.presenceOfElementLocated(locator)) != null) {
            log.info("<<< ELEMENT IS PRESENT: {} >>>", locator);
        } else {
            log.warn("<<< ELEMENT IS NOT PRESENT: {} >>>", locator);
        }
    }

    /**
     * Clicks if the element is visible.
     */
    public boolean clickIfVisible(By locator) {
        if (isElementVisible(locator)) {
            click(locator);
            return true;
        }
        return false;
    }

    /**
     * Waits until the field is visible, clears it and sends text.
     */
    public void type(By locator, String text) {
        try {
            WebElement element = waitToBeVisible(locator);
            element.clear();
            element.sendKeys(text);
            log.info("Text sent to: {}", locator);
        } catch (Exception e) {
            log.error("Error sending text to element: {}", locator, e);
            throw e;
        }
    }

    /**
     * Waits for a loading screen to appear and then disappear.
     */
    public void waitLoadingScreen(String xpath) {
        By loadingIndicator = By.xpath(xpath);
        try {
            log.info("Waiting for loading indicator visibility...");
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

    /**
     * Progressive scroll until an element is found.
     * Once detected in the DOM, centers it in the viewport and clicks it.
     */
    public void scrollUntilFoundAndClick(By locator, int maxScrolls, int stepPx) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        for (int i = 0; i < maxScrolls; i++) {
            try {
                // 1) Find the element in the DOM
                WebElement el = driver.findElement(locator);

                // 2) Wait for visibility (React may take time to render it)
                wait.until(ExpectedConditions.visibilityOf(el));

                // 3) Center in viewport and click
                js.executeScript("arguments[0].scrollIntoView({block:'center', behavior:'instant'});", el);
                Thread.sleep(300);
                el.click();
                log.info("✅ Click performed on {} after {} scrolls", locator, i);
                return;

            } catch (NoSuchElementException e) {
                // Not found yet → keep scrolling down
                js.executeScript("window.scrollBy(0, arguments[0]);", stepPx);
                log.debug("🔽 Scroll {} (+{} px)", i + 1, stepPx);
                try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            } catch (TimeoutException e) {
                log.debug("⌛ Element found but not visible yet (scroll {}), retrying...", i + 1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        throw new NoSuchElementException("❌ Element not found after " + maxScrolls + " scrolls: " + locator);
    }

    // ---------------------------
    // Block: Screenshots and debugging
    // ---------------------------

    /**
     * Takes a screenshot and saves it under a configurable path or /tmp.
     *
     * @return Absolute path to the screenshot file.
     */
    public String takeScreenshot() {
        if (driver == null) {
            log.warn("⚠️ Driver is null. Cannot take screenshot.");
            return null;
        }

        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            // Base path from env var or /tmp
            // - In AWS DF it can be collected as an artifact if configured
            // - In BrowserStack/local: saved under /tmp (or provided path)
            String baseDir = System.getenv("DEVICEFARM_SCREENSHOT_PATH");
            if (baseDir == null || baseDir.isEmpty()) {
                baseDir = "/tmp";
            }

            String filename = "screenshot_" + Instant.now().toEpochMilli() + ".png";

            Path filePath = Path.of(baseDir, filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, screenshot);

            String fullPath = filePath.toAbsolutePath().toString();
            log.info("📸 Screenshot saved at: {}", fullPath);
            return fullPath;

        } catch (IOException e) {
            log.error("💥 Error saving screenshot (Device Farm)", e);
            return null;
        }
    }
}
