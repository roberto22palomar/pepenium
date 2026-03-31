package io.github.roberto22palomar.pepenium.toolkit.support;

import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FastUiSettleTest {

    @Mock
    private AppiumDriver driver;

    @Mock
    private WebElement element;

    @Test
    void waitBrieflyReturnsTrueWhenPageSourceStabilizes() {
        when(driver.findElements(By.className("android.widget.ProgressBar"))).thenReturn(List.of());
        when(driver.findElements(By.className("XCUIElementTypeActivityIndicator"))).thenReturn(List.of());
        when(driver.findElements(By.className("XCUIElementTypeProgressIndicator"))).thenReturn(List.of());
        when(driver.findElements(argThat(by -> by.toString().contains("screen-ready")))).thenReturn(List.of());
        when(driver.getPageSource()).thenReturn("<page>stable</page>");

        FastUiSettle settle = new FastUiSettle(driver,
                Duration.ofMillis(20),
                Duration.ofMillis(1),
                Duration.ofMillis(1));

        assertTrue(settle.waitBriefly());
    }

    @Test
    void waitBrieflyReturnsTrueWhenScreenReadyMarkerIsVisible() {
        when(driver.getPageSource()).thenReturn("");
        when(driver.findElements(argThat(by -> by.toString().contains("screen-ready")))).thenReturn(List.of(element));
        when(element.isDisplayed()).thenReturn(true);

        FastUiSettle settle = new FastUiSettle(driver,
                Duration.ofMillis(20),
                Duration.ofMillis(1),
                Duration.ofMillis(1));

        assertTrue(settle.waitBriefly());
    }

    @Test
    void waitBrieflyReturnsTrueAfterSpinnerDisappearsAndPageStabilizes() {
        when(driver.findElements(By.className("android.widget.ProgressBar"))).thenReturn(List.of(element), List.of());
        when(driver.findElements(By.className("XCUIElementTypeActivityIndicator"))).thenReturn(List.of());
        when(driver.findElements(By.className("XCUIElementTypeProgressIndicator"))).thenReturn(List.of());
        when(driver.findElements(argThat(by -> by.toString().contains("screen-ready")))).thenReturn(List.of());
        when(element.isDisplayed()).thenReturn(true);
        when(driver.getPageSource()).thenReturn("loading", "ready", "ready");

        FastUiSettle settle = new FastUiSettle(driver,
                Duration.ofMillis(20),
                Duration.ofMillis(1),
                Duration.ofMillis(1));

        assertTrue(settle.waitBriefly());
    }

    @Test
    void waitBrieflyReturnsFalseWhenUiKeepsChangingUntilTimeout() {
        when(driver.findElements(By.className("android.widget.ProgressBar"))).thenReturn(List.of());
        when(driver.findElements(By.className("XCUIElementTypeActivityIndicator"))).thenReturn(List.of());
        when(driver.findElements(By.className("XCUIElementTypeProgressIndicator"))).thenReturn(List.of());
        when(driver.findElements(argThat(by -> by.toString().contains("screen-ready")))).thenReturn(List.of());
        AtomicInteger counter = new AtomicInteger();
        when(driver.getPageSource()).thenAnswer(invocation -> "<page>" + counter.incrementAndGet() + "</page>");

        FastUiSettle settle = new FastUiSettle(driver,
                Duration.ofMillis(5),
                Duration.ofMillis(1),
                Duration.ofMillis(1));

        assertFalse(settle.waitBriefly());
    }

    @Test
    void waitBrieflyTreatsPageSourceErrorsAsStableFallback() {
        when(driver.findElements(By.className("android.widget.ProgressBar"))).thenReturn(List.of());
        when(driver.findElements(By.className("XCUIElementTypeActivityIndicator"))).thenReturn(List.of());
        when(driver.findElements(By.className("XCUIElementTypeProgressIndicator"))).thenReturn(List.of());
        when(driver.findElements(argThat(by -> by.toString().contains("screen-ready")))).thenReturn(List.of());
        doThrow(new RuntimeException("no page source")).when(driver).getPageSource();

        FastUiSettle settle = new FastUiSettle(driver,
                Duration.ofMillis(20),
                Duration.ofMillis(1),
                Duration.ofMillis(1));

        assertTrue(settle.waitBriefly());
    }
}
