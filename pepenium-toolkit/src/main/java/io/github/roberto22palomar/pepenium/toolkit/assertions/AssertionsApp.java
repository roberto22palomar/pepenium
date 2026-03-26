package io.github.roberto22palomar.pepenium.toolkit.assertions;

import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.openqa.selenium.By;

public class AssertionsApp extends BaseAssertions {

    public AssertionsApp(AppiumDriver driver) {
        super(driver, "Android");
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
}
