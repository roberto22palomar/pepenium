package io.github.roberto22palomar.pepenium.toolkit.assertions;

import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssertionsWebTest {

    private static final By LOCATOR = By.id("flash");

    @Mock
    private WebDriver driver;

    @Mock
    private WebElement element;

    @AfterEach
    void tearDown() {
        StepTracker.clear();
    }

    @Test
    void assertVisibleRecordsStep() {
        when(driver.findElement(LOCATOR)).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        AssertionsWeb assertions = new AssertionsWeb(driver);
        assertions.assertVisible(LOCATOR);

        assertTrue(StepTracker.snapshot().getSteps().get(0).contains("Assert visible " + LOCATOR));
    }

    @Test
    void assertTextEqualsUsesReadableMessage() {
        when(driver.findElement(LOCATOR)).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);
        when(element.getText()).thenReturn("actual");

        AssertionsWeb assertions = new AssertionsWeb(driver);

        AssertionError error = assertThrows(AssertionError.class,
                () -> assertions.assertTextEquals(LOCATOR, "expected"));

        assertTrue(error.getMessage().contains("Web assertion failed"));
        assertTrue(error.getMessage().contains("expected"));
    }

    @Test
    void assertInputValueEqualsUsesReadableMessage() {
        when(driver.findElement(LOCATOR)).thenReturn(element);
        when(element.getAttribute("value")).thenReturn("actual");

        AssertionsWeb assertions = new AssertionsWeb(driver);

        AssertionError error = assertThrows(AssertionError.class,
                () -> assertions.assertInputValueEquals(LOCATOR, "expected"));

        assertTrue(error.getMessage().contains("Expected input value 'expected'"));
    }
}
