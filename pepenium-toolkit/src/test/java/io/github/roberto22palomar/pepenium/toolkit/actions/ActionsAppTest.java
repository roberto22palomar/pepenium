package io.github.roberto22palomar.pepenium.toolkit.actions;

import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionsAppTest {

    private static final By LOCATOR = By.id("android-field");

    @Mock
    private AppiumDriver driver;

    @Mock
    private WebElement element;

    @Mock
    private WebElement rootElement;

    @AfterEach
    void tearDown() {
        StepTracker.clear();
    }

    @Test
    void clickUsesElementClickAndRecordsStep() {
        when(driver.findElement(LOCATOR)).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);

        ActionsApp actions = new ActionsApp(driver);
        actions.click(LOCATOR);

        verify(element).click();
        assertTrue(StepTracker.snapshot().getSteps().get(0).contains("Tap " + LOCATOR));
    }

    @Test
    void typeClearsAndSendsKeys() {
        when(driver.findElements(any(By.class))).thenReturn(java.util.List.of());
        when(driver.findElement(any(By.class))).thenAnswer(invocation -> {
            By requested = invocation.getArgument(0);
            return requested.equals(LOCATOR) ? element : rootElement;
        });
        when(rootElement.getRect()).thenReturn(new Rectangle(0, 0, 100, 200));

        ActionsApp actions = new ActionsApp(driver);
        actions.type(LOCATOR, "android");

        verify(element).clear();
        verify(element).sendKeys("android");
    }

    @Test
    void compatibilityAliasesStillWork() {
        when(driver.findElements(any(By.class))).thenReturn(java.util.List.of());
        when(driver.findElement(any(By.class))).thenAnswer(invocation -> {
            By requested = invocation.getArgument(0);
            return requested.equals(LOCATOR) ? element : rootElement;
        });
        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);
        when(rootElement.getRect()).thenReturn(new Rectangle(0, 0, 100, 200));
        ActionsApp actions = new ActionsApp(driver);

        actions.makeClick(LOCATOR);
        actions.sendText(LOCATOR, "legacy");

        verify(element).click();
        verify(element).sendKeys("legacy");
    }
}
