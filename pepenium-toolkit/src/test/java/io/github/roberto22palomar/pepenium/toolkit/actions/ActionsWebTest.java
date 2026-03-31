package io.github.roberto22palomar.pepenium.toolkit.actions;

import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class ActionsWebTest {

    private static final By LOCATOR = By.id("username");

    @Mock
    private WebDriver driver;

    @Mock
    private WebElement element;

    @TempDir
    private Path tempDir;

    private String previousTmpDir;

    @BeforeEach
    void captureTmpDir() {
        previousTmpDir = System.getProperty("java.io.tmpdir");
    }

    @AfterEach
    void tearDown() {
        if (previousTmpDir == null) {
            System.clearProperty("java.io.tmpdir");
        } else {
            System.setProperty("java.io.tmpdir", previousTmpDir);
        }
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
    void waitForOpenOverlayUsesExpectedLocator() {
        when(driver.findElement(any(By.class))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        ActionsWeb actions = new ActionsWeb(driver);
        actions.waitForOpenOverlay();

        verify(driver).findElement(argThat(by -> by.toString().contains("sheet-overlay")));
    }

    @Test
    void closeSheetIfOpenClosesOverlayAndRecordsStep() {
        WebElement overlay = mock(WebElement.class);
        WebElement closeButton = mock(WebElement.class);
        when(driver.findElements(argThat(by -> by.toString().contains("sheet-overlay"))))
                .thenReturn(List.of(overlay), List.of());
        when(driver.findElement(argThat(by -> by.toString().contains("sheet-close")))).thenReturn(closeButton);
        when(closeButton.isDisplayed()).thenReturn(true);
        when(closeButton.isEnabled()).thenReturn(true);

        ActionsWeb actions = new ActionsWeb(driver);
        actions.closeSheetIfOpen();

        verify(closeButton).click();
        assertTrue(StepTracker.snapshot().getSteps().stream()
                .anyMatch(step -> step.contains("Close open sheet overlay")));
    }

    @Test
    void waitForAtLeastNElementsWaitsForRequestedCount() {
        WebElement another = mock(WebElement.class);
        when(driver.findElements(LOCATOR)).thenReturn(List.of(element, another));

        ActionsWeb actions = new ActionsWeb(driver);
        actions.waitForAtLeastNElements(LOCATOR, 2);

        verify(driver).findElements(LOCATOR);
    }

    @Test
    void waitToBePresentReturnsLocatedElement() {
        when(driver.findElement(LOCATOR)).thenReturn(element);

        ActionsWeb actions = new ActionsWeb(driver);

        assertSame(element, actions.waitToBePresent(LOCATOR));
    }

    @Test
    void isElementPresentReturnsTrueWhenLocatorExists() {
        when(driver.findElement(LOCATOR)).thenReturn(element);

        ActionsWeb actions = new ActionsWeb(driver);

        assertTrue(actions.isElementPresent(LOCATOR));
    }

    @Test
    void isElementVisibleReturnsTrueWhenElementBecomesVisible() {
        when(driver.findElement(LOCATOR)).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        ActionsWeb actions = new ActionsWeb(driver);

        assertTrue(actions.isElementVisible(LOCATOR));
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

    @Test
    void waitForElementTextReturnsTrueWhenElementMatches() {
        when(driver.findElement(LOCATOR)).thenReturn(element);
        when(element.getText()).thenReturn("ready");

        ActionsWeb actions = new ActionsWeb(driver);

        assertTrue(actions.waitForElementText(LOCATOR, "ready"));
    }

    @Test
    void isElementPresentReturnsFalseWhenLocatorIsMissing() {
        when(driver.findElement(LOCATOR)).thenThrow(new NoSuchElementException("missing"));

        ActionsWeb actions = new ActionsWeb(driver);

        assertFalse(actions.isElementPresent(LOCATOR));
    }

    @Test
    void getElementTextReturnsVisibleElementText() {
        when(driver.findElement(LOCATOR)).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);
        when(element.getText()).thenReturn("pepenium");

        ActionsWeb actions = new ActionsWeb(driver);

        assertEquals("pepenium", actions.getElementText(LOCATOR));
    }

    @Test
    void getElementTextReturnsNullWhenVisibleLookupFails() {
        ActionsWeb actions = new ActionsWeb(driver) {
            @Override
            public WebElement waitToBeVisible(By locator) {
                throw new IllegalStateException("boom");
            }
        };

        assertNull(actions.getElementText(LOCATOR));
    }

    @Test
    void clickIfVisibleReturnsFalseWithoutClickingWhenElementIsHidden() {
        ActionsWeb actions = new ActionsWeb(driver) {
            @Override
            public boolean isElementVisible(By locator) {
                return false;
            }
        };

        assertFalse(actions.clickIfVisible(LOCATOR));
        verifyNoInteractions(driver);
    }

    @Test
    void clickIfVisibleReturnsTrueAndDelegatesToClick() {
        AtomicBoolean clicked = new AtomicBoolean(false);
        ActionsWeb actions = new ActionsWeb(driver) {
            @Override
            public boolean isElementVisible(By locator) {
                return true;
            }

            @Override
            public void click(By locator) {
                clicked.set(true);
            }
        };

        assertTrue(actions.clickIfVisible(LOCATOR));
        assertTrue(clicked.get());
    }

    @Test
    void waitUntilHiddenWaitsForElementToDisappear() {
        when(driver.findElement(LOCATOR))
                .thenReturn(element)
                .thenThrow(new NoSuchElementException("gone"));
        when(element.isDisplayed()).thenReturn(true);

        ActionsWeb actions = new ActionsWeb(driver);
        actions.waitUntilHidden(LOCATOR);

        assertTrue(StepTracker.snapshot().getSteps().stream()
                .anyMatch(step -> step.contains("Wait until hidden " + LOCATOR)));
    }

    @Test
    void takeScreenshotWritesPngFile() throws Exception {
        WebDriver screenshotDriver = mock(WebDriver.class, withSettings().extraInterfaces(TakesScreenshot.class));
        when(((TakesScreenshot) screenshotDriver).getScreenshotAs(OutputType.BYTES))
                .thenReturn("png".getBytes(StandardCharsets.UTF_8));
        System.setProperty("java.io.tmpdir", tempDir.toString());

        ActionsWeb actions = new ActionsWeb(screenshotDriver);
        String screenshotPath = actions.takeScreenshotFast();

        assertNotNull(screenshotPath);
        assertTrue(Files.exists(Path.of(screenshotPath)));
    }

    @Test
    void takeScreenshotReturnsNullWhenDriverIsMissing() {
        ActionsWeb actions = new ActionsWeb(null);

        assertNull(actions.takeScreenshotFast());
    }
}
