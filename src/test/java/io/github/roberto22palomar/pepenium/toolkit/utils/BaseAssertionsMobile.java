package io.github.roberto22palomar.pepenium.toolkit.utils;

import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
abstract class BaseAssertionsMobile {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(6L);

    protected final AppiumDriver driver;
    private final Duration defaultTimeout;

    protected BaseAssertionsMobile(AppiumDriver driver) {
        this(driver, DEFAULT_TIMEOUT);
    }

    protected BaseAssertionsMobile(AppiumDriver driver, Duration defaultTimeout) {
        this.driver = Objects.requireNonNull(driver, "driver cannot be null");
        this.defaultTimeout = Objects.requireNonNull(defaultTimeout, "defaultTimeout cannot be null");
    }

    public WebElement assertVisible(By locator) {
        return assertVisible(locator, defaultTimeout);
    }

    public WebElement assertVisible(By locator, Duration timeout) {
        try {
            WebElement element = wait(timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
            log.debug("Assertion passed: element is visible -> {}", locator);
            return element;
        } catch (TimeoutException e) {
            fail("Expected element to be visible", locator, e);
            return null;
        }
    }

    public void assertNotVisible(By locator) {
        assertNotVisible(locator, defaultTimeout);
    }

    public void assertNotVisible(By locator, Duration timeout) {
        try {
            boolean invisible = wait(timeout).until(ExpectedConditions.invisibilityOfElementLocated(locator));
            if (!invisible) {
                fail("Expected element to be hidden", locator);
            }
            log.debug("Assertion passed: element is not visible -> {}", locator);
        } catch (TimeoutException e) {
            fail("Expected element to stop being visible", locator, e);
        }
    }

    public WebElement assertPresent(By locator) {
        return assertPresent(locator, defaultTimeout);
    }

    public WebElement assertPresent(By locator, Duration timeout) {
        try {
            WebElement element = wait(timeout).until(ExpectedConditions.presenceOfElementLocated(locator));
            log.debug("Assertion passed: element is present -> {}", locator);
            return element;
        } catch (TimeoutException e) {
            fail("Expected element to be present in view hierarchy", locator, e);
            return null;
        }
    }

    public void assertNotPresent(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        if (!elements.isEmpty()) {
            fail("Expected element to not exist in view hierarchy", locator);
        }
        log.debug("Assertion passed: element is not present -> {}", locator);
    }

    public WebElement assertClickable(By locator) {
        return assertClickable(locator, defaultTimeout);
    }

    public WebElement assertClickable(By locator, Duration timeout) {
        try {
            WebElement element = wait(timeout).until(ExpectedConditions.elementToBeClickable(locator));
            log.debug("Assertion passed: element is clickable -> {}", locator);
            return element;
        } catch (TimeoutException e) {
            fail("Expected element to be clickable", locator, e);
            return null;
        }
    }

    public void assertEnabled(By locator) {
        WebElement element = assertPresent(locator);
        if (!element.isEnabled()) {
            fail("Expected element to be enabled", locator);
        }
        log.debug("Assertion passed: element is enabled -> {}", locator);
    }

    public void assertDisabled(By locator) {
        WebElement element = assertPresent(locator);
        if (element.isEnabled()) {
            fail("Expected element to be disabled", locator);
        }
        log.debug("Assertion passed: element is disabled -> {}", locator);
    }

    public void assertSelected(By locator) {
        WebElement element = assertPresent(locator);
        if (!element.isSelected()) {
            fail("Expected element to be selected", locator);
        }
        log.debug("Assertion passed: element is selected -> {}", locator);
    }

    public void assertNotSelected(By locator) {
        WebElement element = assertPresent(locator);
        if (element.isSelected()) {
            fail("Expected element to not be selected", locator);
        }
        log.debug("Assertion passed: element is not selected -> {}", locator);
    }

    public void assertTextEquals(By locator, String expectedText) {
        String actualText = normalizeText(assertVisible(locator).getText());
        if (!Objects.equals(actualText, normalizeText(expectedText))) {
            fail("Expected exact text '" + expectedText + "' but found '" + actualText + "'", locator);
        }
        log.debug("Assertion passed: exact text matches on {}", locator);
    }

    public void assertTextContains(By locator, String expectedText) {
        String actualText = normalizeText(assertVisible(locator).getText());
        if (!actualText.contains(normalizeText(expectedText))) {
            fail("Expected text to contain '" + expectedText + "' but found '" + actualText + "'", locator);
        }
        log.debug("Assertion passed: text contains '{}' on {}", expectedText, locator);
    }

    public void assertTextNotEmpty(By locator) {
        String actualText = normalizeText(assertVisible(locator).getText());
        if (actualText.isEmpty()) {
            fail("Expected element text to not be empty", locator);
        }
        log.debug("Assertion passed: element text is not empty -> {}", locator);
    }

    public void assertValueEquals(By locator, String expectedValue) {
        assertAttributeEquals(locator, "value", expectedValue);
    }

    public void assertAttributePresent(By locator, String attributeName) {
        String actualValue = assertPresent(locator).getAttribute(attributeName);
        if (actualValue == null) {
            fail("Expected attribute '" + attributeName + "' to exist", locator);
        }
        log.debug("Assertion passed: attribute '{}' exists on {}", attributeName, locator);
    }

    public void assertAttributeEquals(By locator, String attributeName, String expectedValue) {
        String actualValue = assertPresent(locator).getAttribute(attributeName);
        if (!Objects.equals(actualValue, expectedValue)) {
            fail(
                    "Expected attribute '" + attributeName + "' to be '" + expectedValue
                            + "' but found '" + actualValue + "'",
                    locator
            );
        }
        log.debug("Assertion passed: attribute '{}' matches on {}", attributeName, locator);
    }

    public void assertAttributeContains(By locator, String attributeName, String expectedPartialValue) {
        String actualValue = assertPresent(locator).getAttribute(attributeName);
        if (actualValue == null || !actualValue.contains(expectedPartialValue)) {
            fail(
                    "Expected attribute '" + attributeName + "' to contain '" + expectedPartialValue
                            + "' but found '" + actualValue + "'",
                    locator
            );
        }
        log.debug("Assertion passed: attribute '{}' contains expected value on {}", attributeName, locator);
    }

    public int assertElementCount(By locator, int expectedCount) {
        return assertElementCount(locator, expectedCount, defaultTimeout);
    }

    public int assertElementCount(By locator, int expectedCount, Duration timeout) {
        int actualCount = waitForElements(locator, timeout).size();
        if (actualCount != expectedCount) {
            fail("Expected " + expectedCount + " elements but found " + actualCount, locator);
        }
        log.debug("Assertion passed: found {} elements for {}", actualCount, locator);
        return actualCount;
    }

    public int assertElementCountAtLeast(By locator, int minimumCount) {
        int actualCount = driver.findElements(locator).size();
        if (actualCount < minimumCount) {
            fail("Expected at least " + minimumCount + " elements but found " + actualCount, locator);
        }
        log.debug("Assertion passed: found at least {} elements for {}", minimumCount, locator);
        return actualCount;
    }

    public int assertElementCountAtMost(By locator, int maximumCount) {
        int actualCount = driver.findElements(locator).size();
        if (actualCount > maximumCount) {
            fail("Expected at most " + maximumCount + " elements but found " + actualCount, locator);
        }
        log.debug("Assertion passed: found at most {} elements for {}", maximumCount, locator);
        return actualCount;
    }

    public void assertAnyElementContainsText(By locator, String expectedText) {
        List<String> texts = waitForElements(locator, defaultTimeout).stream()
                .map(WebElement::getText)
                .map(this::normalizeText)
                .collect(Collectors.toList());

        boolean found = texts.stream().anyMatch(text -> text.contains(normalizeText(expectedText)));
        if (!found) {
            fail(
                    "Expected at least one element to contain '" + expectedText + "' but found: " + texts,
                    locator
            );
        }
        log.debug("Assertion passed: at least one element contains expected text for {}", locator);
    }

    public void assertAllElementsContainText(By locator, String expectedText) {
        List<String> texts = waitForElements(locator, defaultTimeout).stream()
                .map(WebElement::getText)
                .map(this::normalizeText)
                .collect(Collectors.toList());

        boolean allMatch = texts.stream().allMatch(text -> text.contains(normalizeText(expectedText)));
        if (!allMatch) {
            fail("Expected all elements to contain '" + expectedText + "' but found: " + texts, locator);
        }
        log.debug("Assertion passed: all elements contain expected text for {}", locator);
    }

    public void assertStableScreen() {
        if (!isScreenStable()) {
            failDriver("Expected screen to reach a stable state");
        }
        log.debug("Assertion passed: screen is stable");
    }

    protected WebDriverWait wait(Duration timeout) {
        return new WebDriverWait(driver, timeout);
    }

    protected abstract boolean isScreenStable();

    protected abstract String safeScreenshot();

    private List<WebElement> waitForElements(By locator, Duration timeout) {
        try {
            return wait(timeout).until(driver -> {
                List<WebElement> elements = driver.findElements(locator);
                return elements.isEmpty() ? null : elements;
            });
        } catch (TimeoutException e) {
            fail("Expected at least one element to exist", locator, e);
            return Collections.emptyList();
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().replaceAll("\\s+", " ");
    }

    protected void fail(String message, By locator) {
        fail(message, locator, null);
    }

    protected void fail(String message, By locator, Exception cause) {
        String screenshotPath = safeScreenshot();
        String finalMessage = message
                + " | locator=" + locator
                + " | currentContext=" + safeContext()
                + formatScreenshotPath(screenshotPath);

        if (cause != null) {
            log.error(finalMessage, cause);
            Assertions.fail(finalMessage, cause);
            return;
        }

        log.error(finalMessage);
        Assertions.fail(finalMessage);
    }

    protected void failDriver(String message) {
        String screenshotPath = safeScreenshot();
        String finalMessage = message
                + " | currentContext=" + safeContext()
                + formatScreenshotPath(screenshotPath);

        log.error(finalMessage);
        Assertions.fail(finalMessage);
    }

    private String safeContext() {
        try {
            Object platformName = driver.getCapabilities().getCapability("platformName");
            Object automationName = driver.getCapabilities().getCapability("appium:automationName");
            Object appPackage = driver.getCapabilities().getCapability("appium:appPackage");
            return "platform=" + platformName + ", automation=" + automationName + ", appPackage=" + appPackage;
        } catch (Exception e) {
            return "unavailable";
        }
    }

    private String formatScreenshotPath(String screenshotPath) {
        return screenshotPath == null ? "" : " | screenshot=" + screenshotPath;
    }
}
