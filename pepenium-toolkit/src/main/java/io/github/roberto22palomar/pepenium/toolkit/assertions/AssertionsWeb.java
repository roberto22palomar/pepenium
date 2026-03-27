package io.github.roberto22palomar.pepenium.toolkit.assertions;

import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AssertionsWeb extends PlatformAssertions {

    public AssertionsWeb(WebDriver driver) {
        super(driver, "Web");
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
