package io.github.roberto22palomar.pepenium.toolkit.assertions;

import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssertionsAppIOSTest {

    private static final By LOCATOR = By.id("ios-view");

    @Mock
    private AppiumDriver driver;

    @Mock
    private WebElement element;

    @AfterEach
    void tearDown() {
        StepTracker.clear();
    }

    @Test
    void assertVisibleRecordsIOSStep() {
        when(driver.findElement(LOCATOR)).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        AssertionsAppIOS assertions = new AssertionsAppIOS(driver);
        assertions.assertVisible(LOCATOR);

        assertTrue(StepTracker.snapshot().getSteps().get(0).contains("Assert visible " + LOCATOR));
    }
}
