package io.github.roberto22palomar.pepenium.toolkit.assertions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AssertionsWeb extends PlatformAssertions implements WebAssertions {

    public AssertionsWeb(WebDriver driver) {
        super(driver, "Web");
    }

    @Override
    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
            justification = "AssertionsWeb intentionally exposes the active WebDriver for advanced consumer workflows."
    )
    public WebDriver getDriver() {
        return driver;
    }

    @Override
    public void assertUrlContains(String expectedFragment) {
        runAssertion("Assert URL contains " + expectedFragment, () -> {
            try {
                assertionWait().until(d -> {
                    String currentUrl = d.getCurrentUrl();
                    return currentUrl != null && currentUrl.contains(expectedFragment);
                });
            } catch (TimeoutException e) {
                throw assertion("Expected URL containing '" + expectedFragment + "' but found '" + driver.getCurrentUrl() + "'", "currentUrl");
            }
        });
    }

    @Override
    public void assertTitleContains(String expectedFragment) {
        runAssertion("Assert title contains " + expectedFragment, () -> {
            try {
                assertionWait().until(d -> {
                    String title = d.getTitle();
                    return title != null && title.contains(expectedFragment);
                });
            } catch (TimeoutException e) {
                throw assertion("Expected title containing '" + expectedFragment + "' but found '" + driver.getTitle() + "'", "title");
            }
        });
    }

    @Override
    public void assertInputValueEquals(By locator, String expectedValue) {
        runAssertion("Assert input value on " + locator, () -> {
            WebElement element = assertionWait().until(d -> d.findElement(locator));
            String actualValue = element.getAttribute("value");
            if (!expectedValue.equals(actualValue)) {
                throw assertion("Expected input value '" + expectedValue + "' but found '" + actualValue + "'", locator);
            }
        });
    }
}
