package io.github.roberto22palomar.pepenium.toolkit.utils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
public class ActionsAppIOS {

    private final AppiumDriver driver;

    // Ajusta estos tiempos a tu grid (iOS suele necesitar más margen que Android)
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration LONG_TIMEOUT    = Duration.ofSeconds(25);
    private static final Duration POLLING         = Duration.ofMillis(60);

    public AppiumDriver getDriver() { return this.driver; }

    // =========================
    // Esperas (helpers internos)
    // =========================
    private WebDriverWait waitShort() {
        WebDriverWait w = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        w.pollingEvery(POLLING);
        w.ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        return w;
    }

    private WebDriverWait waitLong() {
        WebDriverWait w = new WebDriverWait(driver, LONG_TIMEOUT);
        w.pollingEvery(POLLING);
        w.ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        return w;
    }

    private <T> T untilShort(Function<? super WebDriver, T> condition) {
        return waitShort().until(condition);
    }

    private <T> T untilLong(Function<? super WebDriver, T> condition) {
        return waitLong().until(condition);
    }

    // =========================
    // Esperas y validaciones
    // =========================

    /** Visible en pantalla (no sólo presente en el árbol). */
    public WebElement esperarVisible(By locator) {
        log.info("<<< WAIT visible: {} >>>", locator);
        return untilLong(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Presente (sin exigir visibilidad). Útil para colecciones o cargas perezosas. */
    public WebElement esperarPresente(By locator) {
        log.info("<<< WAIT presente: {} >>>", locator);
        return untilLong(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /** Clickable ≈ visible + enabled (ojo: en iOS no siempre es perfecto, pero ayuda). */
    public WebElement esperarClickable(By locator) {
        log.info("<<< WAIT clickable: {} >>>", locator);
        return untilLong(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Devuelve true si el elemento aparece visible dentro del timeout corto. */
    public boolean estaElementoVisible(By locator) {
        try {
            untilShort(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            log.debug("No visible a tiempo: {}", locator);
            return false;
        }
    }

    /** Espera a que el texto visible del elemento contenga la cadena. */
    public boolean waitForElementText(By locator, String textoEsperado) {
        try {
            return untilShort(ExpectedConditions.textToBePresentInElementLocated(locator, textoEsperado));
        } catch (TimeoutException e) {
            log.warn("Timeout esperando texto '{}' en: {}", textoEsperado, locator);
            return false;
        }
    }

    /** Espera a que el locator desaparezca (invisible o no presente). */
    public boolean waitGone(By locator) {
        try {
            return untilLong(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            log.warn("Timeout esperando que desaparezca: {}", locator);
            return false;
        }
    }

    /** Obtiene el texto de un elemento visible (reduce Stale con un re-locate). */
    public String getElementText(By locator) {
        try {
            WebElement el = esperarVisible(locator);
            return el.getText();
        } catch (Exception e) {
            log.error("Error obteniendo texto: {}", locator, e);
            return null;
        }
    }

    // =========================
    // Interacciones básicas
    // =========================
    @SneakyThrows
    public void hacerClick(By locator) {
        try {
            WebElement el = esperarClickable(locator);
            el.click();
            log.info("CLICK en: {}", locator);
        } catch (TimeoutException e) {
            log.error("Timeout al hacer clic en: {}", locator, e);
            throw e;
        } catch (ElementClickInterceptedException ice) {
            // Reintento suave: pequeño tap W3C en el centro del elemento
            log.warn("Interceptado el click, intento tap W3C: {}", locator);
            WebElement el = esperarVisible(locator);
            Rectangle r = el.getRect();
            int cx = r.getX() + r.getWidth()/2;
            int cy = r.getY() + r.getHeight()/2;
            tapPoint(cx, cy, 80);
        } catch (Exception e) {
            log.error("Error al hacer click en: {}", locator, e);
            throw e;
        }
    }

    public boolean hacerClickSiVisible(By locator) {
        if (estaElementoVisible(locator)) {
            hacerClick(locator);
            return true;
        }
        return false;
    }

    public void enviarTexto(By locator, String texto) {
        try {
            esperarPantallaEstable();
            WebElement el = esperarVisible(locator);
            el.clear();
            el.sendKeys(texto);
            // En iOS a veces el teclado tapa el botón siguiente:
            try { /*driver.hideKeyboard();*/ } catch (Exception ignore) {}
            log.info("Texto enviado a {}: '{}'", locator, texto);
        } catch (Exception e) {
            log.error("Error al enviar texto a: {}", locator, e);
            throw e;
        }
    }

    /** Espera a un “loader” custom: pásame el locator (predicate o id) y espero a que aparezca y luego desaparezca. */
    public void esperarPantallaCargaDesaparezca(By loadingLocator) {
        log.info("<<< WAIT loader visible: {} >>>", loadingLocator);
        try {
            untilLong(ExpectedConditions.visibilityOfElementLocated(loadingLocator));
        } catch (TimeoutException e) {
            log.warn("Loader no llegó a aparecer (puede ser OK): {}", loadingLocator);
        }
        log.info("<<< WAIT loader gone: {} >>>", loadingLocator);
        waitGone(loadingLocator);
    }

    // =========================
    // Estabilidad de pantalla (iOS)
    // =========================
    public boolean esperarPantallaEstable() {
        final By ROOT    = AppiumBy.iOSClassChain("**/XCUIElementTypeWindow[1]");
        final By SPINNER = AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeActivityIndicator' AND visible == 1");

        long fin = System.nanoTime() + Duration.ofSeconds(3).toNanos();
        Rectangle anterior = null;
        int establesSeguidos = 0;

        while (System.nanoTime() < fin) {
            try {
                // Si hay spinner visible, resetea contador
                if (!driver.findElements(SPINNER).isEmpty()) {
                    establesSeguidos = 0;
                    Thread.sleep(200);
                    continue;
                }

                Rectangle actual = driver.findElement(ROOT).getRect();
                if (actual.equals(anterior)) {
                    establesSeguidos++;
                    if (establesSeguidos >= 3) return true; // ~600-800ms estables
                } else {
                    establesSeguidos = 0;
                }
                anterior = actual;
                Thread.sleep(250);

            } catch (StaleElementReferenceException e) {
                establesSeguidos = 0;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception any) {
                // Fallback a viewport
                Dimension size = driver.manage().window().getSize();
                Rectangle fallback = new Rectangle(new Point(0,0), size);
                if (fallback.equals(anterior)) {
                    establesSeguidos++;
                    if (establesSeguidos >= 3) return true;
                } else {
                    establesSeguidos = 0;
                }
                anterior = fallback;
            }
        }
        return establesSeguidos >= 1; // al menos algo de estabilidad
    }

    // =========================
    // Gestos y scroll
    // =========================
    public void swipeUp() {
        esperarPantallaEstable();
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.85);
        int endY   = (int) (size.getHeight() * 0.20);
        performSwipe(new Point(x, startY), new Point(x, endY), 500);
    }

    public void swipeDown() {
        esperarPantallaEstable();
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.20);
        int endY   = (int) (size.getHeight() * 0.85);
        performSwipe(new Point(x, startY), new Point(x, endY), 500);
        hacerCapturaPantalla();
    }

    public void swipeLeft() {
        esperarPantallaEstable();
        Dimension size = driver.manage().window().getSize();
        int y = size.getHeight() / 2;
        int startX = (int) (size.getWidth() * 0.85);
        int endX   = (int) (size.getWidth() * 0.15);
        performSwipe(new Point(startX, y), new Point(endX, y), 500);
    }

    public void swipeRight() {
        esperarPantallaEstable();
        Dimension size = driver.manage().window().getSize();
        int y = size.getHeight() / 2;
        int startX = (int) (size.getWidth() * 0.15);
        int endX   = (int) (size.getWidth() * 0.85);
        performSwipe(new Point(startX, y), new Point(endX, y), 500);
        hacerCapturaPantalla();
    }

    /** Scroll por swipes repetidos hasta que el elemento sea visible o agote intentos. */
    public WebElement scrollToElement(By locator, int maxSwipes) {
        int attempts = 0;
        while (attempts < maxSwipes) {
            try {
                WebElement el = driver.findElement(locator);
                if (el.isDisplayed()) return el;
            } catch (Exception ignored) {
                // no-op
            }
            swipeUp();
            attempts++;
        }
        throw new NoSuchElementException("No se encontró tras " + maxSwipes + " swipes: " + locator);
    }

    // =========================
    // Capturas / Debug
    // =========================
    public String hacerCapturaPantalla() {
        esperarPantallaEstable();
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            String filename = "screenshot_" + Instant.now().toEpochMilli() + ".png";
            Path filePath = Path.of("/tmp", filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, screenshot);
            String fullPath = filePath.toAbsolutePath().toString();
            log.info("📸 Captura guardada en: {}", fullPath);
            return fullPath;
        } catch (IOException e) {
            log.error("Error guardando captura", e);
            return null;
        } catch (Exception e) {
            log.error("Error inesperado capturando pantalla", e);
            return null;
        }
    }

    public void tapCenter() {
        Dimension size = driver.manage().window().getSize();
        tapPoint(size.width / 2, size.height / 2, 80);
    }

    private void tapPoint(int x, int y, int ms) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, Duration.ofMillis(ms)))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(tap));
    }

    public void scrollToElementSeguro(By locator) {
        ScrollUtils scroller = new ScrollUtils(driver);
        scroller.scrollToElement(locator, 12);
    }

    // === SWIPE EN LA ZONA DE UN ELEMENTO ===
    public void swipeAtElement(By locator,
                               Direction direction,
                               int times,
                               double percent,
                               int durationMs) {
        esperarPantallaEstable();
        WebElement el = esperarVisible(locator);
        Rectangle r = el.getRect();

        int cx = r.getX() + r.getWidth() / 2;
        int cy = r.getY() + r.getHeight() / 2;

        int dy = (int) Math.max(1, r.getHeight() * percent);
        int dx = (int) Math.max(1, r.getWidth()  * percent);

        for (int i = 0; i < times; i++) {
            int startX = cx, startY = cy, endX = cx, endY = cy;
            switch (direction) {
                case UP:    endY = cy - dy; break;
                case DOWN:  endY = cy + dy; break;
                case LEFT:  endX = cx - dx; break;
                case RIGHT: endX = cx + dx; break;
            }
            Point start = clampToViewport(startX, startY);
            Point end   = clampToViewport(endX, endY);
            performSwipe(start, end, durationMs);
            log.info("Swipe {} en {} ({} -> {})", direction, locator, start, end);
        }
    }

    private void performSwipe(Point start, Point end, int durationMs) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), start.getX(), start.getY()))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(durationMs),
                        PointerInput.Origin.viewport(), end.getX(), end.getY()))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(swipe));
    }

    private void performSwipeIOS(WebElement el, Point start, Point end, int durationMs) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

        // Asegura duración mínima para que iOS detecte el gesto
        int safeDuration = Math.max(durationMs, 400);

        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), start.getX(), start.getY()))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(safeDuration),
                        PointerInput.Origin.viewport(), end.getX(), end.getY()))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }

    private Point clampToViewportIOS(int x, int y) {
        Dimension size = driver.manage().window().getSize();

        // Margen mayor en iOS para evitar gestos del sistema
        int margin = 20;

        int safeX = Math.max(margin, Math.min(size.getWidth()  - margin, x));
        int safeY = Math.max(margin, Math.min(size.getHeight() - margin, y));

        return new Point(safeX, safeY);
    }

    public enum Direction { UP, DOWN, LEFT, RIGHT }


    /**
     * Realiza un swipe sobre un elemento en iOS (driver XCUITest).
     */
    public void swipeAtElementIOS(By locator,
                                  Direction direction,
                                  int times,
                                  double percent,        // 0.0 - 1.0 del alto/ancho del elemento
                                  int durationMs) {      // duración del gesto

        esperarPantallaEstable();
        WebElement el = esperarVisible(locator);
        Rectangle r = el.getRect();

        // Centro del elemento
        int cx = r.getX() + r.getWidth() / 2;
        int cy = r.getY() + r.getHeight() / 2;

        // Distancia de swipe (más suave y segura en iOS)
        int dy = (int) Math.max(10, r.getHeight() * percent);
        int dx = (int) Math.max(10, r.getWidth()  * percent);

        for (int i = 0; i < times; i++) {
            int startX = cx, startY = cy, endX = cx, endY = cy;

            switch (direction) {
                case UP:    endY = cy - dy; break;
                case DOWN:  endY = cy + dy; break;
                case LEFT:  endX = cx - dx; break;
                case RIGHT: endX = cx + dx; break;
            }

            Point start = clampToViewportIOS(startX, startY);
            Point end   = clampToViewportIOS(endX, endY);

            performSwipeIOS(el, start, end, durationMs);
            log.info("📱 Swipe {} (iOS) en {} -> {} → {}", direction, locator, start, end);
        }
    }


    private Point clampToViewport(int x, int y) {
        Dimension size = driver.manage().window().getSize();
        int safeX = Math.max(5, Math.min(size.getWidth()  - 5, x));
        int safeY = Math.max(5, Math.min(size.getHeight() - 5, y));
        return new Point(safeX, safeY);
    }

}
