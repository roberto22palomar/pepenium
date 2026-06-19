package io.github.roberto22palomar.pepenium.toolkit.support;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "The Appium driver is a shared mutable runtime handle intentionally owned elsewhere."
)
public final class FastUiSettle {
    private final AppiumDriver driver;
    private final Duration maxWait;
    private final Duration poll;
    private final Duration settle;

    public FastUiSettle(AppiumDriver driver) {
        this(driver, Duration.ofMillis(650), Duration.ofMillis(120), Duration.ofMillis(80));
    }

    public FastUiSettle(AppiumDriver driver, Duration maxWait, Duration poll, Duration settle) {
        this.driver = driver;
        this.maxWait = maxWait;
        this.poll = poll;
        this.settle = settle;
    }

    public boolean waitBriefly() {
        long deadline = System.nanoTime() + maxWait.toNanos();
        String previousHash = safeHash(pageSourceSafe());
        boolean observedMovement = false;

        while (System.nanoTime() < deadline) {
            if (hasScreenReadyFlag()) {
                return sleep(settle);
            }

            if (hasGenericSpinner()) {
                observedMovement = true;
                if (!sleep(poll)) {
                    return false;
                }
                previousHash = safeHash(pageSourceSafe());
                continue;
            }

            if (!sleep(poll)) {
                return false;
            }
            String currentHash = safeHash(pageSourceSafe());
            if (currentHash.equals(previousHash)) {
                if (observedMovement) {
                    return sleep(settle);
                }
                return true;
            }

            observedMovement = true;
            previousHash = currentHash;
        }

        return false;
    }

    private boolean hasGenericSpinner() {
        try {
            for (WebElement element : elements(By.className("android.widget.ProgressBar"))) {
                if (element.isDisplayed()) {
                    return true;
                }
            }

            for (WebElement element : elements(By.className("XCUIElementTypeActivityIndicator"))) {
                if (element.isDisplayed()) {
                    return true;
                }
            }

            for (WebElement element : elements(By.className("XCUIElementTypeProgressIndicator"))) {
                if (element.isDisplayed()) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Unable to inspect spinner state while settling screenshot", e);
        }
        return false;
    }

    private boolean hasScreenReadyFlag() {
        try {
            for (WebElement element : elements(By.xpath("//*[@resource-id='screen-ready']"))) {
                if (element.isDisplayed()) {
                    log.debug("Screen-ready marker detected before screenshot");
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Unable to inspect screen-ready marker while settling screenshot", e);
        }
        return false;
    }

    private String pageSourceSafe() {
        try {
            String source = driver.getPageSource();
            return source == null ? "" : source;
        } catch (Exception e) {
            return "";
        }
    }

    private List<WebElement> elements(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        return elements == null ? Collections.emptyList() : elements;
    }

    private String safeHash(String source) {
        int length = Math.min(source.length(), 5000);
        return Integer.toHexString(source.substring(0, length).hashCode());
    }

    private boolean sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
