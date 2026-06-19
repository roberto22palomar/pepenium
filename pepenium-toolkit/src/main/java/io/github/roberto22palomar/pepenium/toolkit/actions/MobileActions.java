package io.github.roberto22palomar.pepenium.toolkit.actions;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Common action surface implemented by Android and iOS action helpers.
 */
public interface MobileActions {

    AppiumDriver getDriver();

    WebElement waitToBePresent(By locator);

    WebElement waitToBeVisible(By locator);

    WebElement waitToBeClickable(By locator);

    boolean waitForElementText(By locator, String expectedText);

    boolean isElementPresent(By locator);

    boolean isElementVisible(By locator);

    boolean waitGone(By locator);

    String getElementText(By locator);

    void click(By locator);

    boolean clickIfVisible(By locator);

    void type(By locator, String text);

    void waitUntilHidden(By locator);

    boolean waitStableScreen();

    void swipeUp();

    void swipeDown();

    void swipeLeft();

    void swipeRight();

    WebElement scrollToElement(By locator, int maxSwipes);

    void tapCenter();

    void swipeAtElement(By locator, SwipeDirection direction, int times, double percent, int durationMs);

    String takeScreenshotFast();

    String takeScreenshot();

    String takeScreenshot(boolean settleBeforeCapture);
}
