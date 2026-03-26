package io.github.roberto22palomar.pepenium.toolkit.support;

import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
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

    @Test
    void scrollToElementReturnsImmediatelyVisibleElement() {
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("platformName", "android");

        when(driver.getCapabilities()).thenReturn(capabilities);
        when(driver.manage()).thenReturn(options);
        when(options.timeouts()).thenReturn(timeouts);
        when(timeouts.getImplicitWaitTimeout()).thenReturn(Duration.ZERO);
        when(timeouts.implicitlyWait(Duration.ofMillis(200))).thenReturn(timeouts);
        when(timeouts.implicitlyWait(Duration.ZERO)).thenReturn(timeouts);
        when(driver.findElements(LOCATOR)).thenReturn(List.of(element));
        when(element.isDisplayed()).thenReturn(true);

        ScrollUtils scrollUtils = new ScrollUtils(driver);
        WebElement result = scrollUtils.scrollToElement(LOCATOR, 3);

        assertSame(element, result);
        verify(timeouts).implicitlyWait(Duration.ofMillis(200));
        verify(timeouts).implicitlyWait(Duration.ZERO);
    }
}
