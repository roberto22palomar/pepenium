package io.github.roberto22palomar.pepenium.toolkit.assertions;

import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AssertionsWeb extends BaseAssertions {

    public AssertionsWeb(WebDriver driver) {
        super(driver, "Web");
    }

    public void assertVisible(By locator) {
        StepTracker.record("Assert visible " + locator);
        super.assertVisible(locator);
    }

    public void assertNotVisible(By locator) {
        StepTracker.record("Assert hidden " + locator);
        super.assertNotVisible(locator);
    }

    public void assertPresent(By locator) {
        StepTracker.record("Assert present " + locator);
        super.assertPresent(locator);
    }

    public void assertTextEquals(By locator, String expectedText) {
        StepTracker.record("Assert exact text on " + locator);
        super.assertTextEquals(locator, expectedText);
    }

    public void assertTextContains(By locator, String expectedFragment) {
        StepTracker.record("Assert partial text on " + locator);
        super.assertTextContains(locator, expectedFragment);
    }

    public void assertCountAtLeast(By locator, int minimumCount) {
        StepTracker.record("Assert count >= " + minimumCount + " for " + locator);
        int actual = count(locator);
        if (actual < minimumCount) {
            throw assertion("Expected at least " + minimumCount + " elements but found " + actual, locator);
        }
    }

    public void assertUrlContains(String expectedFragment) {
        StepTracker.record("Assert URL contains " + expectedFragment);
        try {
            assertionWait().until(d -> {
                String currentUrl = d.getCurrentUrl();
                return currentUrl != null && currentUrl.contains(expectedFragment);
            });
        } catch (TimeoutException e) {
            throw assertion("Expected URL containing '" + expectedFragment + "' but found '" + driver.getCurrentUrl() + "'", "currentUrl");
        }
    }

    public void assertTitleContains(String expectedFragment) {
        StepTracker.record("Assert title contains " + expectedFragment);
        try {
            assertionWait().until(d -> {
                String title = d.getTitle();
                return title != null && title.contains(expectedFragment);
            });
        } catch (TimeoutException e) {
            throw assertion("Expected title containing '" + expectedFragment + "' but found '" + driver.getTitle() + "'", "title");
        }
    }

    public void assertInputValueEquals(By locator, String expectedValue) {
        StepTracker.record("Assert input value on " + locator);
        WebElement element = assertionWait().until(d -> d.findElement(locator));
        String actualValue = element.getAttribute("value");
        if (!expectedValue.equals(actualValue)) {
            throw assertion("Expected input value '" + expectedValue + "' but found '" + actualValue + "'", locator);
        }
    }
}
