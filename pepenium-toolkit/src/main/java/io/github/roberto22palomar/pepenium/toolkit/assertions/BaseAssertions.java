package io.github.roberto22palomar.pepenium.toolkit.assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

abstract class BaseAssertions {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(6);

    protected final WebDriver driver;
    private final String platformName;

    protected BaseAssertions(WebDriver driver, String platformName) {
        this.driver = driver;
        this.platformName = platformName;
    }

    protected void assertVisible(By locator) {
        try {
            assertionWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw assertion("Expected visible element but it was not visible", locator);
        }
    }

    protected void assertVisible(WebElement element) {
        try {
            assertionWait().until(ExpectedConditions.visibilityOf(element));
        } catch (TimeoutException e) {
            throw assertion("Expected visible element but it was not visible", element);
        }
    }

    protected void assertNotVisible(By locator) {
        try {
            boolean hidden = assertionWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
            if (!hidden) {
                throw assertion("Expected element to be hidden", locator);
            }
        } catch (TimeoutException e) {
            throw assertion("Expected element to become hidden", locator);
        }
    }

    protected void assertNotVisible(WebElement element) {
        try {
            boolean hidden = assertionWait().until(ExpectedConditions.invisibilityOf(element));
            if (!hidden) {
                throw assertion("Expected element to be hidden", element);
            }
        } catch (TimeoutException e) {
            throw assertion("Expected element to become hidden", element);
        }
    }

    protected void assertPresent(By locator) {
        try {
            assertionWait().until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw assertion("Expected present element but it was not found", locator);
        }
    }

    protected void assertPresent(WebElement element) {
        try {
            assertionWait().until(d -> element != null);
        } catch (TimeoutException e) {
            throw assertion("Expected present element but it was not found", element);
        }
    }

    protected void assertTextEquals(By locator, String expectedText) {
        String actualText = readText(locator);
        if (!expectedText.equals(actualText)) {
            throw assertion(
                    "Expected text '" + expectedText + "' but found '" + actualText + "'",
                    locator
            );
        }
    }

    protected void assertTextEquals(WebElement element, String expectedText) {
        String actualText = readText(element);
        if (!expectedText.equals(actualText)) {
            throw assertion(
                    "Expected text '" + expectedText + "' but found '" + actualText + "'",
                    element
            );
        }
    }

    protected void assertTextContains(By locator, String expectedFragment) {
        String actualText = readText(locator);
        if (actualText == null || !actualText.contains(expectedFragment)) {
            throw assertion(
                    "Expected text containing '" + expectedFragment + "' but found '" + actualText + "'",
                    locator
            );
        }
    }

    protected void assertTextContains(WebElement element, String expectedFragment) {
        String actualText = readText(element);
        if (actualText == null || !actualText.contains(expectedFragment)) {
            throw assertion(
                    "Expected text containing '" + expectedFragment + "' but found '" + actualText + "'",
                    element
            );
        }
    }

    protected String readText(By locator) {
        try {
            WebElement element = assertionWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
            return element.getText();
        } catch (TimeoutException e) {
            throw assertion("Expected visible element to read text from", locator);
        }
    }

    protected String readText(WebElement element) {
        try {
            WebElement visibleElement = assertionWait().until(ExpectedConditions.visibilityOf(element));
            return visibleElement.getText();
        } catch (TimeoutException e) {
            throw assertion("Expected visible element to read text from", element);
        }
    }

    protected int count(By locator) {
        return driver.findElements(locator).size();
    }

    protected WebDriverWait assertionWait() {
        return new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    protected AssertionError assertion(String message, Object target) {
        return new AssertionError(platformName + " assertion failed: " + message + " [" + target + "]");
    }
}
