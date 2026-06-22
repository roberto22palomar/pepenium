package io.github.roberto22palomar.pepenium.toolkit.assertions;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public interface MobileAssertions {

    AppiumDriver getDriver();

    void assertVisible(By locator);

    void assertVisible(WebElement element);

    void assertNotVisible(By locator);

    void assertNotVisible(WebElement element);

    void assertPresent(By locator);

    void assertPresent(WebElement element);

    void assertTextEquals(By locator, String expectedText);

    void assertTextEquals(WebElement element, String expectedText);

    void assertTextContains(By locator, String expectedFragment);

    void assertTextContains(WebElement element, String expectedFragment);
}
