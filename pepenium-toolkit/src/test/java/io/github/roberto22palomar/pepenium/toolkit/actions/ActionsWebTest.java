package io.github.roberto22palomar.pepenium.toolkit.actions;

import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionsWebTest {

    private static final By LOCATOR = By.id("username");

    @Mock
    private WebDriver driver;

    @Mock
    private WebElement element;

    @AfterEach
    void tearDown() {
        StepTracker.clear();
    }

    @Test
    void clickPerformsClickAndRecordsStep() {
        when(driver.findElement(LOCATOR)).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);

        ActionsWeb actions = new ActionsWeb(driver);
        actions.click(LOCATOR);

        verify(element).click();
        assertEquals(1, StepTracker.snapshot().getTotalRecorded());
        assertTrue(StepTracker.snapshot().getSteps().get(0).contains("Click " + LOCATOR));
    }

    @Test
    void typeClearsAndSendsKeys() {
        when(driver.findElement(LOCATOR)).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        ActionsWeb actions = new ActionsWeb(driver);
        actions.type(LOCATOR, "pepenium");

        verify(element).clear();
        verify(element).sendKeys("pepenium");
    }
}
