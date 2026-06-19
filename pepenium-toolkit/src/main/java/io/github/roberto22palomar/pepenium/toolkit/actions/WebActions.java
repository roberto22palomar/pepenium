package io.github.roberto22palomar.pepenium.toolkit.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface WebActions {

    WebDriver getDriver();

    void waitForOpenOverlay();

    void closeSheetIfOpen();

    void waitForAtLeastNElements(By locator, int n);

    WebElement waitToBeVisible(By locator);

    WebElement waitToBeVisible(WebElement element);

    WebElement waitToBePresent(By locator);

    boolean waitForElementText(By locator, String expectedText);

    boolean isElementPresent(By locator);

    boolean isElementVisible(By locator);

    boolean isElementVisible(WebElement element);

    boolean waitGone(By locator);

    String getElementText(By locator);

    String getElementText(WebElement element);

    void click(By locator);

    void click(WebElement element);

    boolean clickIfVisible(By locator);

    boolean clickIfVisible(WebElement element);

    void type(By locator, String text);

    void type(WebElement element, String text);

    void waitUntilHidden(By locator);

    String takeScreenshotFast();

    String takeScreenshot();

    String takeScreenshot(boolean settleBeforeCapture);
}
