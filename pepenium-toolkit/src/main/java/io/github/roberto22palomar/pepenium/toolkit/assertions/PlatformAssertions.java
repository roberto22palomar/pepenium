package io.github.roberto22palomar.pepenium.toolkit.assertions;

import io.github.roberto22palomar.pepenium.core.observability.PepeniumTimeline;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

abstract class PlatformAssertions extends BaseAssertions {

    protected PlatformAssertions(WebDriver driver, String platformName) {
        super(driver, platformName);
    }

    public void assertVisible(By locator) {
        runAssertion("Assert visible " + locator, () -> super.assertVisible(locator));
    }

    public void assertNotVisible(By locator) {
        runAssertion("Assert hidden " + locator, () -> super.assertNotVisible(locator));
    }

    public void assertPresent(By locator) {
        runAssertion("Assert present " + locator, () -> super.assertPresent(locator));
    }

    public void assertTextEquals(By locator, String expectedText) {
        runAssertion("Assert exact text on " + locator, () -> super.assertTextEquals(locator, expectedText));
    }

    public void assertTextContains(By locator, String expectedFragment) {
        runAssertion("Assert partial text on " + locator, () -> super.assertTextContains(locator, expectedFragment));
    }

    protected void recordPassedAssertion(String description) {
        StepTracker.record(description);
        PepeniumTimeline.recordAssertionPassed(description);
    }

    protected void recordFailedAssertion(String description) {
        StepTracker.record(description);
        PepeniumTimeline.recordAssertionFailed(description);
    }

    protected void runAssertion(String description, ThrowingRunnable runnable) {
        try {
            runnable.run();
            recordPassedAssertion(description);
        } catch (RuntimeException | AssertionError e) {
            recordFailedAssertion(description);
            throw e;
        }
    }

    @FunctionalInterface
    protected interface ThrowingRunnable {
        void run();
    }
}
