package io.github.roberto22palomar.pepenium.toolkit.utils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScrollUtils {

    private final AppiumDriver driver;

    public ScrollUtils(AppiumDriver driver) { this.driver = driver; }

    public WebElement scrollToElement(By locator, int maxSwipes) {
        Objects.requireNonNull(locator, "locator no puede ser null");
        if (maxSwipes < 1) maxSwipes = 8; // valor razonable por defecto

        String platform = String.valueOf(driver.getCapabilities().getCapability("platformName")).toLowerCase(Locale.ROOT);
        boolean isAndroid = platform.contains("android");
        boolean isIOS = platform.contains("ios");

        // Implicit breve dentro del bucle
        Duration originalImplicit = Duration.ZERO;
        try { originalImplicit = driver.manage().timeouts().getImplicitWaitTimeout(); } catch (Throwable ignored) {}
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(200));

        try {
            // Intento inmediato
            List<WebElement> now = driver.findElements(locator);
            for (WebElement el : now) if (el.isDisplayed()) return el;

            if (isAndroid) {
                // ===== ANDROID: intentamos UiScrollable (ultra-fiable y NO toca bordes del SO) =====
                Optional<String> uiSelectorTarget = toUiSelector(locator);
                if (uiSelectorTarget.isPresent()) {
                    String script = "new UiScrollable(new UiSelector().scrollable(true))"
                            + ".setAsVerticalList().scrollIntoView(" + uiSelectorTarget.get() + ");";
                    try {
                        return driver.findElement(AppiumBy.androidUIAutomator(script));
                    } catch (NoSuchElementException ignored) {
                        // Si no lo encuentra, caemos al fallback con swipes seguros
                    } catch (Exception e) {
                        // Algunos drivers antiguos pueden fallar; seguimos al fallback
                    }
                }
            }

            // ===== Fallback universal seguro (Android/iOS): swipes dentro de zona segura =====
            int attempts = 0;
            int stagnant = 0;
            int lastHash = 0;
            while (attempts < maxSwipes) {
                // Buscar tras cada swipe
                List<WebElement> list = driver.findElements(locator);
                for (WebElement el : list) {
                    try { if (el.isDisplayed()) return el; } catch (StaleElementReferenceException ignored) {}
                }

                boolean moved = swipeUpSafe(); // “dedo sube” → contenido baja (ver abajo)
                int hash = safePageHash();
                stagnant = (hash == lastHash) ? (stagnant + 1) : 0;
                lastHash = hash;

                // Si “no se mueve” en 2 iteraciones seguidas, asumimos fin de lista
                if (!moved || stagnant >= 2) break;

                attempts++;
            }

            // Última búsqueda
            List<WebElement> last = driver.findElements(locator);
            for (WebElement el : last) if (el.isDisplayed()) return el;

            throw new NoSuchElementException("No se encontró el elemento tras " + Math.max(1, maxSwipes)
                    + " swipes (o fin de lista): " + locator);

        } finally {
            try { driver.manage().timeouts().implicitlyWait(originalImplicit); } catch (Throwable ignored) {}
        }
    }

    // ------------------ Helpers ------------------

    /** Convierte By a UiSelector (resourceId / text / textContains). */
    private Optional<String> toUiSelector(By locator) {
        String s = locator.toString(); // p.ej. "By.id: com.foo:id/row" o "By.xpath: //*[@resource-id='com.foo:id/row']"
        // By.id →
        if (s.startsWith("By.id: ")) {
            String id = s.substring("By.id: ".length()).trim();
            return Optional.of("new UiSelector().resourceId(\"" + id + "\")");
        }
        // XPath con @resource-id →
        Matcher mId = Pattern.compile("@resource-id\\s*=\\s*'([^']+)'").matcher(s);
        if (mId.find()) {
            return Optional.of("new UiSelector().resourceId(\"" + mId.group(1) + "\")");
        }
        // XPath con text()/contains(text(), …) →
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

    /** Swipe “seguro” dentro de una zona central del viewport (evita status/gesture bars del SO). */
    private boolean swipeUpSafe() {
        try {
            Dimension vp = driver.manage().window().getSize();
            int left   = (int) (vp.width  * 0.08);
            int right  = (int) (vp.width  * 0.92);
            int top    = (int) (vp.height * 0.18);  // lejos de la barra de estado
            int bottom = (int) (vp.height * 0.82);

            int startX = (left + right) / 2;
            int startY = (int) (bottom - (bottom - top) * 0.10);
            int endY   = (int) (top + (bottom - top) * 0.10);

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
        try { return driver.getPageSource().hashCode(); } catch (Throwable t) { return new Random().nextInt(); }
    }
}
