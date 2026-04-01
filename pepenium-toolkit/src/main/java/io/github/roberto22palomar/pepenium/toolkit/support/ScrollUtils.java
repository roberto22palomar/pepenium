package io.github.roberto22palomar.pepenium.toolkit.support;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "The Appium driver is a shared mutable runtime handle intentionally owned elsewhere."
)
public class ScrollUtils {

    private final AppiumDriver driver;

    public ScrollUtils(AppiumDriver driver) {
        this.driver = driver;
    }

    public WebElement scrollToElement(By locator, int maxSwipes) {
        Objects.requireNonNull(locator, "locator must not be null");
        if (maxSwipes < 1) {
            maxSwipes = 8; // reasonable default
        }

        String platform = String.valueOf(driver.getCapabilities().getCapability("platformName")).toLowerCase(Locale.ROOT);
        boolean isAndroid = platform.contains("android");

        // Keep a short implicit wait inside the scroll loop.
        Duration originalImplicit = Duration.ZERO;
        try {
            originalImplicit = driver.manage().timeouts().getImplicitWaitTimeout();
        } catch (Throwable ignored) {
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(200));

        try {
            // Immediate attempt before any scrolling.
            List<WebElement> now = driver.findElements(locator);
            for (WebElement el : now) {
                if (el.isDisplayed()) {
                    return el;
                }
            }

            if (isAndroid) {
                // ===== ANDROID: try UiScrollable first (reliable and avoids OS gesture edges) =====
                Optional<String> uiSelectorTarget = toUiSelector(locator);
                if (uiSelectorTarget.isPresent()) {
                    String script = "new UiScrollable(new UiSelector().scrollable(true))"
                            + ".setAsVerticalList().scrollIntoView(" + uiSelectorTarget.get() + ");";
                    try {
                        return driver.findElement(AppiumBy.androidUIAutomator(script));
                    } catch (NoSuchElementException ignored) {
                        // If not found, fall back to safe swipes.
                    } catch (Exception e) {
                        // Some older drivers may fail here; continue with the fallback.
                    }
                }
            }

            // ===== Universal fallback (Android/iOS): swipe only inside a safe central zone =====
            int attempts = 0;
            int stagnant = 0;
            int lastHash = 0;
            while (attempts < maxSwipes) {
                // Retry after each swipe.
                List<WebElement> list = driver.findElements(locator);
                for (WebElement el : list) {
                    try {
                        if (el.isDisplayed()) {
                            return el;
                        }
                    } catch (StaleElementReferenceException ignored) {
                    }
                }

                boolean moved = swipeUpSafe(); // Finger moves up -> content moves down.
                int hash = safePageHash();
                stagnant = (hash == lastHash) ? (stagnant + 1) : 0;
                lastHash = hash;

                // If the page does not move for two consecutive iterations, assume end of list.
                if (!moved || stagnant >= 2) {
                    break;
                }

                attempts++;
            }

            // Final lookup after the scroll attempts.
            List<WebElement> last = driver.findElements(locator);
            for (WebElement el : last) {
                if (el.isDisplayed()) {
                    return el;
                }
            }

            throw new NoSuchElementException("Element not found after " + Math.max(1, maxSwipes)
                    + " swipes (or end of list): " + locator);

        } finally {
            try {
                driver.manage().timeouts().implicitlyWait(originalImplicit);
            } catch (Throwable ignored) {
            }
        }
    }

    // ------------------ Helpers ------------------

    /** Converts a Selenium locator into a UiSelector when possible. */
    private Optional<String> toUiSelector(By locator) {
        String s = locator.toString(); // e.g. "By.id: com.foo:id/row" or "By.xpath: //*[@resource-id='com.foo:id/row']"
        // By.id ->
        if (s.startsWith("By.id: ")) {
            String id = s.substring("By.id: ".length()).trim();
            return Optional.of("new UiSelector().resourceId(\"" + id + "\")");
        }
        // XPath with @resource-id ->
        Matcher mId = Pattern.compile("@resource-id\\s*=\\s*'([^']+)'").matcher(s);
        if (mId.find()) {
            return Optional.of("new UiSelector().resourceId(\"" + mId.group(1) + "\")");
        }
        // XPath with text()/contains(text(), ...) ->
        Matcher mText = Pattern.compile("text\\(\\)\\s*=\\s*'([^']+)'").matcher(s);
        if (mText.find()) {
            return Optional.of("new UiSelector().text(\"" + mText.group(1) + "\")");
        }
        Matcher mTextContains = Pattern.compile("contains\\(text\\(\\),\\s*'([^']+)'\\)").matcher(s);
        if (mTextContains.find()) {
            return Optional.of("new UiSelector().textContains(\"" + mTextContains.group(1) + "\")");
        }
        return Optional.empty();
    }

    /** Swipes inside a central safe zone to avoid OS status and gesture bars. */
    private boolean swipeUpSafe() {
        try {
            Dimension vp = driver.manage().window().getSize();
            int left = (int) (vp.width * 0.08);
            int right = (int) (vp.width * 0.92);
            int top = (int) (vp.height * 0.18); // far enough from the status bar
            int bottom = (int) (vp.height * 0.82);

            int startX = (left + right) / 2;
            int startY = (int) (bottom - (bottom - top) * 0.10);
            int endY = (int) (top + (bottom - top) * 0.10);

            org.openqa.selenium.interactions.PointerInput finger =
                    new org.openqa.selenium.interactions.PointerInput(org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
            org.openqa.selenium.interactions.Sequence swipe = new org.openqa.selenium.interactions.Sequence(finger, 1);
            swipe.addAction(finger.createPointerMove(Duration.ZERO, org.openqa.selenium.interactions.PointerInput.Origin.viewport(), startX, startY));
            swipe.addAction(finger.createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(350), org.openqa.selenium.interactions.PointerInput.Origin.viewport(), startX, endY));
            swipe.addAction(finger.createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Collections.singletonList(swipe));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int safePageHash() {
        try {
            return driver.getPageSource().hashCode();
        } catch (Throwable t) {
            return ThreadLocalRandom.current().nextInt();
        }
    }
}
