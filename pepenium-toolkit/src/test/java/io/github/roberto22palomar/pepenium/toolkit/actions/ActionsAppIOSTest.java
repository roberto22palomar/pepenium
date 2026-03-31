package io.github.roberto22palomar.pepenium.toolkit.actions;

import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionsAppIOSTest {

    private static final By LOCATOR = By.id("ios-field");

    @Mock
    private AppiumDriver driver;

    @Mock
    private WebElement element;

    @Mock
    private WebElement rootElement;

    @Mock
    private WebDriver.Options options;

    @Mock
    private WebDriver.Window window;

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
    void clickUsesElementClickAndRecordsStep() {
        when(driver.findElement(LOCATOR)).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);
        when(element.isEnabled()).thenReturn(true);

        ActionsAppIOS actions = new ActionsAppIOS(driver);
        actions.click(LOCATOR);

        verify(element).click();
        assertTrue(StepTracker.snapshot().getSteps().get(0).contains("Tap " + LOCATOR));
    }

    @Test
    void getDriverReturnsInjectedDriver() {
        ActionsAppIOS actions = new ActionsAppIOS(driver);

        assertSame(driver, actions.getDriver());
    }

    @Test
    void typeClearsAndSendsKeys() {
        when(driver.findElements(any(By.class))).thenReturn(List.of());
        when(driver.findElement(any(By.class))).thenAnswer(invocation -> {
            By requested = invocation.getArgument(0);
            return requested.equals(LOCATOR) ? element : rootElement;
        });
        when(element.isDisplayed()).thenReturn(true);
        when(rootElement.getRect()).thenReturn(new Rectangle(0, 0, 100, 200));

        ActionsAppIOS actions = new ActionsAppIOS(driver);
        actions.type(LOCATOR, "ios");

        verify(element).clear();
        verify(element).sendKeys("ios");
    }

    @Test
    void clickFallsBackToW3cTapWhenClickIsIntercepted() {
        when(element.getRect()).thenReturn(new Rectangle(10, 20, 30, 40));
        doThrow(new ElementClickInterceptedException("busy")).when(element).click();

        ActionsAppIOS actions = new ActionsAppIOS(driver) {
            @Override
            public WebElement waitToBeClickable(By locator) {
                return element;
            }

            @Override
            public WebElement waitToBeVisible(By locator) {
                return element;
            }
        };

        actions.click(LOCATOR);

        verify(driver).perform(anyList());
    }

    @Test
    void waitStableScreenUsesViewportFallbackWhenRootLookupFails() {
        when(driver.findElements(any(By.class))).thenReturn(List.of(), List.of(), List.of());
        when(driver.findElement(any(By.class))).thenThrow(new RuntimeException("not ready"));
        when(driver.manage()).thenReturn(options);
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenReturn(new Dimension(200, 400));

        ActionsAppIOS actions = new ActionsAppIOS(driver);

        assertTrue(actions.waitStableScreen());
    }

    @Test
    void swipeUpPerformsGestureAcrossViewport() {
        when(driver.manage()).thenReturn(options);
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenReturn(new Dimension(200, 400));

        ActionsAppIOS actions = new ActionsAppIOS(driver) {
            @Override
            public boolean waitStableScreen() {
                return true;
            }
        };

        actions.swipeUp();

        verify(driver).perform(anyList());
    }

    @Test
    void swipeDownCapturesScreenshotAfterGesture() {
        when(driver.manage()).thenReturn(options);
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenReturn(new Dimension(200, 400));
        AtomicInteger screenshots = new AtomicInteger();

        ActionsAppIOS actions = new ActionsAppIOS(driver) {
            @Override
            public boolean waitStableScreen() {
                return true;
            }

            @Override
            public String takeScreenshot() {
                screenshots.incrementAndGet();
                return "shot";
            }
        };

        actions.swipeDown();

        verify(driver).perform(anyList());
        assertEquals(1, screenshots.get());
    }

    @Test
    void swipeRightCapturesScreenshotAfterGesture() {
        when(driver.manage()).thenReturn(options);
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenReturn(new Dimension(200, 400));
        AtomicInteger screenshots = new AtomicInteger();

        ActionsAppIOS actions = new ActionsAppIOS(driver) {
            @Override
            public boolean waitStableScreen() {
                return true;
            }

            @Override
            public String takeScreenshot() {
                screenshots.incrementAndGet();
                return "shot";
            }
        };

        actions.swipeRight();

        verify(driver).perform(anyList());
        assertEquals(1, screenshots.get());
    }

    @Test
    void scrollToElementRetriesUntilElementIsVisible() {
        when(driver.findElement(LOCATOR))
                .thenThrow(new NoSuchElementException("missing"))
                .thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);
        AtomicInteger swipes = new AtomicInteger();

        ActionsAppIOS actions = new ActionsAppIOS(driver) {
            @Override
            public void swipeUp() {
                swipes.incrementAndGet();
            }
        };

        WebElement found = actions.scrollToElement(LOCATOR, 2);

        assertSame(element, found);
        assertEquals(1, swipes.get());
    }

    @Test
    void tapCenterUsesViewportCenterPoint() {
        when(driver.manage()).thenReturn(options);
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenReturn(new Dimension(200, 400));

        ActionsAppIOS actions = new ActionsAppIOS(driver);
        actions.tapCenter();

        verify(driver).perform(anyList());
    }

    @Test
    void swipeAtElementPerformsRequestedSwipes() {
        when(element.getRect()).thenReturn(new Rectangle(95, 95, 20, 20));
        when(driver.manage()).thenReturn(options);
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenReturn(new Dimension(100, 100));

        ActionsAppIOS actions = new ActionsAppIOS(driver) {
            @Override
            public boolean waitStableScreen() {
                return true;
            }

            @Override
            public WebElement waitToBeVisible(By locator) {
                return element;
            }
        };

        actions.swipeAtElement(LOCATOR, ActionsAppIOS.Direction.LEFT, 2, 1.0, 150);

        verify(driver, times(2)).perform(anyList());
    }

    @Test
    void clickIfVisibleReturnsFalseWhenElementIsHidden() {
        ActionsAppIOS actions = new ActionsAppIOS(driver) {
            @Override
            public boolean isElementVisible(By locator) {
                return false;
            }
        };

        assertFalse(actions.clickIfVisible(LOCATOR));
    }

    @Test
    void takeScreenshotFastWritesPngFile() throws Exception {
        when(driver.getScreenshotAs(OutputType.BYTES)).thenReturn("png".getBytes(StandardCharsets.UTF_8));
        System.setProperty("java.io.tmpdir", tempDir.toString());

        ActionsAppIOS actions = new ActionsAppIOS(driver);
        String screenshotPath = actions.takeScreenshotFast();

        assertNotNull(screenshotPath);
        assertTrue(Files.exists(Path.of(screenshotPath)));
    }
}
