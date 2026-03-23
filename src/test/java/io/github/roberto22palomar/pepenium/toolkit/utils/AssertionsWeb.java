package io.github.roberto22palomar.pepenium.toolkit.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class AssertionsWeb {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(6L);

    private final WebDriver driver;
    private final ActionsWeb actionsWeb;
    private final Duration defaultTimeout;

    public AssertionsWeb(WebDriver driver) {
        this(driver, new ActionsWeb(driver), DEFAULT_TIMEOUT);
    }

    public AssertionsWeb(WebDriver driver, ActionsWeb actionsWeb) {
        this(driver, actionsWeb, DEFAULT_TIMEOUT);
    }

    public AssertionsWeb(WebDriver driver, ActionsWeb actionsWeb, Duration defaultTimeout) {
        this.driver = Objects.requireNonNull(driver, "driver cannot be null");
        this.actionsWeb = Objects.requireNonNull(actionsWeb, "actionsWeb cannot be null");
        this.defaultTimeout = Objects.requireNonNull(defaultTimeout, "defaultTimeout cannot be null");
    }

    public WebElement assertVisible(By locator) {
        return assertVisible(locator, defaultTimeout);
    }

    public WebElement assertVisible(By locator, Duration timeout) {
        try {
            WebElement element = wait(timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
            log.info("Assertion passed: element is visible -> {}", locator);
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
            log.info("Assertion passed: element is not visible -> {}", locator);
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
            log.info("Assertion passed: element is present -> {}", locator);
            return element;
        } catch (TimeoutException e) {
            fail("Expected element to be present in DOM", locator, e);
            return null;
        }
    }

    public void assertNotPresent(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        if (!elements.isEmpty()) {
            fail("Expected element to not exist in DOM", locator);
        }
        log.info("Assertion passed: element is not present -> {}", locator);
    }

    public WebElement assertClickable(By locator) {
        return assertClickable(locator, defaultTimeout);
    }

    public WebElement assertClickable(By locator, Duration timeout) {
        try {
            WebElement element = wait(timeout).until(ExpectedConditions.elementToBeClickable(locator));
            log.info("Assertion passed: element is clickable -> {}", locator);
            return element;
        } catch (TimeoutException e) {
            fail("Expected element to be clickable", locator, e);
            return null;
        }
    }

    public void assertEnabled(By locator) {
        WebElement element = assertVisible(locator);
        if (!element.isEnabled()) {
            fail("Expected element to be enabled", locator);
        }
        log.info("Assertion passed: element is enabled -> {}", locator);
    }

    public void assertDisabled(By locator) {
        WebElement element = assertPresent(locator);
        if (element.isEnabled()) {
            fail("Expected element to be disabled", locator);
        }
        log.info("Assertion passed: element is disabled -> {}", locator);
    }

    public void assertSelected(By locator) {
        WebElement element = assertPresent(locator);
        if (!element.isSelected()) {
            fail("Expected element to be selected", locator);
        }
        log.info("Assertion passed: element is selected -> {}", locator);
    }

    public void assertNotSelected(By locator) {
        WebElement element = assertPresent(locator);
        if (element.isSelected()) {
            fail("Expected element to not be selected", locator);
        }
        log.info("Assertion passed: element is not selected -> {}", locator);
    }

    public void assertTextEquals(By locator, String expectedText) {
        String actualText = normalizeText(assertVisible(locator).getText());
        if (!Objects.equals(actualText, normalizeText(expectedText))) {
            fail("Expected exact text '" + expectedText + "' but found '" + actualText + "'", locator);
        }
        log.info("Assertion passed: exact text matches on {}", locator);
    }

    public void assertTextContains(By locator, String expectedText) {
        String actualText = normalizeText(assertVisible(locator).getText());
        if (!actualText.contains(normalizeText(expectedText))) {
            fail("Expected text to contain '" + expectedText + "' but found '" + actualText + "'", locator);
        }
        log.info("Assertion passed: text contains '{}' on {}", expectedText, locator);
    }

    public void assertTextNotEmpty(By locator) {
        String actualText = normalizeText(assertVisible(locator).getText());
        if (actualText.isEmpty()) {
            fail("Expected element text to not be empty", locator);
        }
        log.info("Assertion passed: element text is not empty -> {}", locator);
    }

    public void assertValueEquals(By locator, String expectedValue) {
        assertAttributeEquals(locator, "value", expectedValue);
    }

    public void assertAttributePresent(By locator, String attributeName) {
        String actualValue = assertPresent(locator).getAttribute(attributeName);
        if (actualValue == null) {
            fail("Expected attribute '" + attributeName + "' to exist", locator);
        }
        log.info("Assertion passed: attribute '{}' exists on {}", attributeName, locator);
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
        log.info("Assertion passed: attribute '{}' matches on {}", attributeName, locator);
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
        log.info("Assertion passed: attribute '{}' contains expected value on {}", attributeName, locator);
    }

    public void assertCssValueEquals(By locator, String cssProperty, String expectedValue) {
        String actualValue = assertVisible(locator).getCssValue(cssProperty);
        if (!Objects.equals(actualValue, expectedValue)) {
            fail(
                    "Expected css property '" + cssProperty + "' to be '" + expectedValue
                            + "' but found '" + actualValue + "'",
                    locator
            );
        }
        log.info("Assertion passed: css property '{}' matches on {}", cssProperty, locator);
    }

    public int assertElementCount(By locator, int expectedCount) {
        return assertElementCount(locator, expectedCount, defaultTimeout);
    }

    public int assertElementCount(By locator, int expectedCount, Duration timeout) {
        int actualCount = waitForElements(locator, timeout).size();
        if (actualCount != expectedCount) {
            fail("Expected " + expectedCount + " elements but found " + actualCount, locator);
        }
        log.info("Assertion passed: found {} elements for {}", actualCount, locator);
        return actualCount;
    }

    public int assertElementCountAtLeast(By locator, int minimumCount) {
        int actualCount = driver.findElements(locator).size();
        if (actualCount < minimumCount) {
            fail("Expected at least " + minimumCount + " elements but found " + actualCount, locator);
        }
        log.info("Assertion passed: found at least {} elements for {}", minimumCount, locator);
        return actualCount;
    }

    public int assertElementCountAtMost(By locator, int maximumCount) {
        int actualCount = driver.findElements(locator).size();
        if (actualCount > maximumCount) {
            fail("Expected at most " + maximumCount + " elements but found " + actualCount, locator);
        }
        log.info("Assertion passed: found at most {} elements for {}", maximumCount, locator);
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
        log.info("Assertion passed: at least one element contains expected text for {}", locator);
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
        log.info("Assertion passed: all elements contain expected text for {}", locator);
    }

    public void assertUrlEquals(String expectedUrl) {
        String currentUrl = driver.getCurrentUrl();
        if (!Objects.equals(currentUrl, expectedUrl)) {
            failPage("Expected current URL to be '" + expectedUrl + "' but found '" + currentUrl + "'");
        }
        log.info("Assertion passed: current URL matches -> {}", expectedUrl);
    }

    public void assertUrlContains(String expectedFragment) {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl == null || !currentUrl.contains(expectedFragment)) {
            failPage("Expected current URL to contain '" + expectedFragment + "' but found '" + currentUrl + "'");
        }
        log.info("Assertion passed: current URL contains -> {}", expectedFragment);
    }

    public void assertPathEquals(String expectedPath) {
        String currentPath = URI.create(driver.getCurrentUrl()).getPath();
        if (!Objects.equals(currentPath, expectedPath)) {
            failPage("Expected current path to be '" + expectedPath + "' but found '" + currentPath + "'");
        }
        log.info("Assertion passed: current path matches -> {}", expectedPath);
    }

    public void assertTitleEquals(String expectedTitle) {
        String actualTitle = driver.getTitle();
        if (!Objects.equals(actualTitle, expectedTitle)) {
            failPage("Expected page title to be '" + expectedTitle + "' but found '" + actualTitle + "'");
        }
        log.info("Assertion passed: page title matches -> {}", expectedTitle);
    }

    public void assertTitleContains(String expectedFragment) {
        String actualTitle = driver.getTitle();
        if (actualTitle == null || !actualTitle.contains(expectedFragment)) {
            failPage("Expected page title to contain '" + expectedFragment + "' but found '" + actualTitle + "'");
        }
        log.info("Assertion passed: page title contains -> {}", expectedFragment);
    }

    public void assertDocumentReady() {
        Object readyState = ((JavascriptExecutor) driver).executeScript("return document.readyState");
        if (!"complete".equals(String.valueOf(readyState))) {
            failPage("Expected document.readyState to be 'complete' but found '" + readyState + "'");
        }
        log.info("Assertion passed: document.readyState is complete");
    }

    public void assertCurrentUrlMatchesBase(String expectedBaseUrl) {
        URI current = URI.create(driver.getCurrentUrl());
        URI expected = URI.create(expectedBaseUrl);

        boolean matches = Objects.equals(current.getScheme(), expected.getScheme())
                && Objects.equals(current.getHost(), expected.getHost())
                && resolvePort(current) == resolvePort(expected);

        if (!matches) {
            failPage(
                    "Expected browser to stay on base URL '" + expectedBaseUrl
                            + "' but found '" + driver.getCurrentUrl() + "'"
            );
        }
        log.info("Assertion passed: browser stays on expected base URL -> {}", expectedBaseUrl);
    }

    private WebDriverWait wait(Duration timeout) {
        return new WebDriverWait(driver, timeout);
    }

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

    private int resolvePort(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        if ("https".equalsIgnoreCase(uri.getScheme())) {
            return 443;
        }
        if ("http".equalsIgnoreCase(uri.getScheme())) {
            return 80;
        }
        return -1;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().replaceAll("\\s+", " ");
    }

    private void fail(String message, By locator) {
        fail(message, locator, null);
    }

    private void fail(String message, By locator, Exception cause) {
        String screenshotPath = safeScreenshot();
        String finalMessage = message
                + " | locator=" + locator
                + " | currentUrl=" + safeCurrentUrl()
                + formatScreenshotPath(screenshotPath);

        if (cause != null) {
            log.error(finalMessage, cause);
            Assertions.fail(finalMessage, cause);
            return;
        }

        log.error(finalMessage);
        Assertions.fail(finalMessage);
    }

    private void failPage(String message) {
        String screenshotPath = safeScreenshot();
        String finalMessage = message
                + " | currentUrl=" + safeCurrentUrl()
                + formatScreenshotPath(screenshotPath);

        log.error(finalMessage);
        Assertions.fail(finalMessage);
    }

    private String safeScreenshot() {
        try {
            return actionsWeb.takeScreenshot();
        } catch (NoSuchElementException e) {
            log.warn("Could not take screenshot because the page state is unstable", e);
            return null;
        } catch (Exception e) {
            log.warn("Could not take screenshot for assertion context", e);
            return null;
        }
    }

    private String safeCurrentUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            return "unavailable";
        }
    }

    private String formatScreenshotPath(String screenshotPath) {
        return screenshotPath == null ? "" : " | screenshot=" + screenshotPath;
    }
}
