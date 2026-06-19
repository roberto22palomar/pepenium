package io.github.roberto22palomar.pepenium.toolkit.assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface WebAssertions {

    WebDriver getDriver();

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

    void assertUrlContains(String expectedFragment);

    void assertTitleContains(String expectedFragment);

    void assertInputValueEquals(By locator, String expectedValue);
}
