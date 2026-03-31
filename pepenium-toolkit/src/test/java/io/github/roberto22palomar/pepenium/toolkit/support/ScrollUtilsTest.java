package io.github.roberto22palomar.pepenium.toolkit.support;

import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScrollUtilsTest {

    private static final By LOCATOR = By.id("row");

    @Mock
    private AppiumDriver driver;

    @Mock
    private WebElement element;

    @Mock
    private WebDriver.Options options;

    @Mock
    private WebDriver.Timeouts timeouts;

    @Mock
    private WebDriver.Window window;

    private MutableCapabilities capabilities;

    private void stubDriverState() {
        capabilities = new MutableCapabilities();
        when(driver.getCapabilities()).thenReturn(capabilities);
        when(driver.manage()).thenReturn(options);
        when(options.timeouts()).thenReturn(timeouts);
        when(timeouts.implicitlyWait(Duration.ofMillis(200))).thenReturn(timeouts);
        when(timeouts.implicitlyWait(Duration.ZERO)).thenReturn(timeouts);
        when(timeouts.getImplicitWaitTimeout()).thenReturn(Duration.ZERO);
    }

    private void stubViewport() {
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenReturn(new Dimension(1080, 1920));
    }

    @Test
    void scrollToElementReturnsImmediatelyVisibleElement() {
        stubDriverState();
        capabilities.setCapability("platformName", "android");
        when(driver.findElements(LOCATOR)).thenReturn(List.of(element));
        when(element.isDisplayed()).thenReturn(true);

        ScrollUtils scrollUtils = new ScrollUtils(driver);
        WebElement result = scrollUtils.scrollToElement(LOCATOR, 3);

        assertSame(element, result);
        verify(timeouts).implicitlyWait(Duration.ofMillis(200));
        verify(timeouts).implicitlyWait(Duration.ZERO);
    }

    @Test
    void scrollToElementRejectsNullLocator() {
        ScrollUtils scrollUtils = new ScrollUtils(driver);

        NullPointerException error = assertThrows(NullPointerException.class,
                () -> scrollUtils.scrollToElement(null, 2));

        assertTrue(error.getMessage().contains("locator"));
    }

    @Test
    void scrollToElementUsesUiScrollableForAndroidIdLocator() {
        stubDriverState();
        capabilities.setCapability("platformName", "android");
        By resourceId = By.id("com.foo:id/row");

        when(driver.findElements(resourceId)).thenReturn(List.of());
        when(driver.findElement(argThat(by -> by.toString().contains("resourceId(\"com.foo:id/row\")"))))
                .thenReturn(element);

        ScrollUtils scrollUtils = new ScrollUtils(driver);

        assertSame(element, scrollUtils.scrollToElement(resourceId, 3));
    }

    @Test
    void scrollToElementUsesUiScrollableForAndroidXpathTextContainsLocator() {
        stubDriverState();
        capabilities.setCapability("platformName", "android");
        By textLocator = By.xpath("//*[contains(text(),'Pepenium')]");

        when(driver.findElements(textLocator)).thenReturn(List.of());
        when(driver.findElement(argThat(by -> by.toString().contains("textContains(\"Pepenium\")"))))
                .thenReturn(element);

        ScrollUtils scrollUtils = new ScrollUtils(driver);

        assertSame(element, scrollUtils.scrollToElement(textLocator, 3));
    }

    @Test
    void scrollToElementFallsBackToSafeSwipeWhenElementAppearsAfterScroll() {
        stubDriverState();
        stubViewport();
        capabilities.setCapability("platformName", "iOS");
        when(driver.findElements(LOCATOR)).thenReturn(List.of(), List.of(), List.of(element));
        when(driver.getPageSource()).thenReturn("page-a");
        when(element.isDisplayed()).thenReturn(true);

        ScrollUtils scrollUtils = new ScrollUtils(driver);
        WebElement result = scrollUtils.scrollToElement(LOCATOR, 2);

        assertSame(element, result);
        verify(driver).perform(anyList());
    }

    @Test
    void scrollToElementStopsAfterStagnantPageAndThrows() {
        stubDriverState();
        stubViewport();
        capabilities.setCapability("platformName", "iOS");
        when(driver.findElements(LOCATOR)).thenReturn(List.of(), List.of(), List.of(), List.of());
        when(driver.getPageSource()).thenReturn("same-page");

        ScrollUtils scrollUtils = new ScrollUtils(driver);

        assertThrows(NoSuchElementException.class, () -> scrollUtils.scrollToElement(LOCATOR, 3));
        verify(driver, atLeastOnce()).perform(anyList());
    }
}
