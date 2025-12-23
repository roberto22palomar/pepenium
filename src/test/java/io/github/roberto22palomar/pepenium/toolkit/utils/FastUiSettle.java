package io.github.roberto22palomar.pepenium.toolkit.utils;

import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;


@Slf4j
/**
 * Espera ultrarrápida: si aparece el flag "screen-ready", disparamos.
 * Si no, vigilamos spinners y, como fallback, comprobamos estabilidad del DOM (pageSource hash).
 */
public final class FastUiSettle {
    private final AppiumDriver driver;
    private final Duration settle = Duration.ofMillis(150);        // micro-settle tras condición OK
    private final Duration spinnerWait = Duration.ofMillis(5000);  // tope si hay loader
    private final Duration poll = Duration.ofMillis(120);          // polling

    public FastUiSettle(AppiumDriver driver) {
        this.driver = driver;
    }

    public void waitBriefly() {
        // 0) Si ya está el flag "screen-ready" → listo
       /* if (hasScreenReadyFlag()) {
            sleep(settle);
            return;
        }*/

        // 1) Si no hay spinner → settle corto
        /*if (!hasGenericSpinner()) {
            sleep(settle);
            return;
        }
*/
        // 2) Hay spinner → esperar a que desaparezca o a que aparezca el flag
     /*   long end = System.nanoTime() + spinnerWait.toNanos();
        while (System.nanoTime() < end) {
            if (!hasGenericSpinner() || hasScreenReadyFlag()) {
                sleep(settle);
                return;
            }
            sleep(poll);
        }*/

        // 3) Fallback: 2 hashes de pageSource separados 100ms (máx ~200ms)
        String h1 = safeHash(pageSourceSafe());
        sleep(Duration.ofMillis(100));
        String h2 = safeHash(pageSourceSafe());
        if (!h1.equals(h2)) sleep(settle);
    }

    /**
     * Devuelve true si detecta un indicador genérico de carga (Android/iOS).
     */
    private boolean hasGenericSpinner() {
        try {
            // Android
            for (WebElement e : driver.findElements(By.className("android.widget.ProgressBar")))
                if (e.isDisplayed()) return true;
            // iOS
            for (WebElement e : driver.findElements(By.className("XCUIElementTypeActivityIndicator")))
                if (e.isDisplayed()) return true;
            for (WebElement e : driver.findElements(By.className("XCUIElementTypeProgressIndicator")))
                if (e.isDisplayed()) return true;
        } catch (Exception ignore) {
            log.error("Error al comprobar generic spinner en screenshots.");
        }
        return false;
    }

    /**
     * Atajo positivo: si existe el flag "screen-ready", consideramos la pantalla lista.
     */
    private boolean hasScreenReadyFlag() {
        try {
            // Android: resource-id
            for (WebElement e : driver.findElements(By.xpath("//*[@resource-id='screen-ready']")))
                if (e.isDisplayed()) {
                    log.info("SCREEN READY SE ENCONTRÓ");
                    return true;
                }

        } catch (Exception ignore) {
            log.error("Error al comprobar generic spinner en screenshots.");
        }
        return false;
    }

    private String pageSourceSafe() {
        try {
            return driver.getPageSource();
        } catch (Exception e) {
            return "";
        }
    }

    private String safeHash(String s) {
        int len = Math.min(s.length(), 5000); // hash sobre un trozo para evitar coste excesivo
        return Integer.toHexString(s.substring(0, len).hashCode());
    }

    private void sleep(Duration d) {
        try {
            Thread.sleep(d.toMillis());
        } catch (InterruptedException ignored) {
            log.error("Error dormir el hilo en screenshots.");
        }
    }
}
