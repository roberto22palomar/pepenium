package io.github.roberto22palomar.pepenium.toolkit.utils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
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

@RequiredArgsConstructor
@Slf4j
public class ActionsApp {

    private final AppiumDriver driver;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(6L);
    private static final Duration LONG_TIMEOUT = Duration.ofSeconds(120L);

    // ---------------------------
    // Bloque: Esperas y validaciones
    // ---------------------------

    /**
     * Espera hasta que el elemento esté visible en pantalla.
     */
    public WebElement esperarVisible(By locator) {
        log.info("<<< ESPERANDO QUE ESTÉ PRESENTE EL ELEMENTO: {}", locator);
        WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

        if (element != null) {
            log.info("<<< EL ELEMENTO ESTÁ PRESENTE: {}", locator);
        } else {
            log.warn("<<< EL ELEMENTO NO ESTÁ PRESENTE: {}", locator);
        }
        return element;
    }

    /**
     * Espera hasta que el texto del elemento sea el esperado.
     */
    public boolean waitForElementText(By locator, String textoEsperado) {
        try {
            return new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.textToBe(locator, textoEsperado));
        } catch (TimeoutException e) {
            log.warn("Timeout esperando texto '{}' en elemento: {}", textoEsperado, locator);
            return false;
        }
    }

    /**
     * Verifica si un elemento está presente sin esperar.
     */
    public boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Verifica si un elemento está visible.
     */
    public boolean estaElementoVisible(By locator) {
        try {
            new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            log.warn("Elemento no visible: {}", locator);
            return false;
        }
    }

    /**
     * Obtiene el texto visible de un elemento.
     */
    public String getElementText(By locator) {
        try {
            WebElement elemento = esperarVisible(locator);
            return elemento.getText();
        } catch (Exception e) {
            log.error("Error obteniendo texto de elemento: {}", locator, e);
            return null;
        }
    }

    // ---------------------------
    // Bloque: Interacciones básicas
    // ---------------------------

    /**
     * Espera hasta que el elemento sea clicable y hace clic.
     */
    @SneakyThrows
    public void hacerClick(By locator) {
        try {

            WebElement elemento = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(locator));
            elemento.click();
            log.info("CLICK hecho en: {}", locator);
        } catch (TimeoutException e) {
            log.error("Timeout al hacer clic en el elemento: {}", locator, e);
            throw e;
        } catch (Exception e) {
            log.error("Error al hacer clic en el elemento: {}", locator, e);
            throw e;
        }
    }

    public boolean esperarPantallaEstable() {
        final By ROOT = AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"android:id/content\")");
        final By SPINNER = AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.ProgressBar\")");

        long fin = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        org.openqa.selenium.Rectangle anterior = null;
        int establesSeguidos = 2;

        while (System.nanoTime() < fin) {
            try {
                // Si hay loader visible, no consideramos estable
                if (!driver.findElements(SPINNER).isEmpty()) {
                    establesSeguidos = 0;
                    Thread.sleep(1500);
                    continue;
                }

                org.openqa.selenium.Rectangle actual = driver.findElement(ROOT).getRect();

                if (actual.equals(anterior)) {
                    if (++establesSeguidos >= 3) return true; // ✔ estable
                } else {
                    establesSeguidos = 0;
                }

                anterior = actual;
                Thread.sleep(1500);

            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                establesSeguidos = 0; // el root se recreó → reintentar
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false; // no llegó a estabilizarse a tiempo
    }


    /**
     * Hace clic si el elemento está visible.
     */
    public boolean hacerClickSiVisible(By locator) throws InterruptedException {
        if (estaElementoVisible(locator)) {
            hacerClick(locator);
            return true;
        }
        return false;
    }

    /**
     * Espera hasta que el campo sea visible, lo limpia y envía texto.
     */
    public void enviarTexto(By locator, String texto) {
        try {
            esperarPantallaEstable();
            WebElement elemento = esperarVisible(locator);
            elemento.clear();
            elemento.sendKeys(texto);
            log.info("Texto enviado a: {}", locator);
        } catch (Exception e) {
            log.error("Error al enviar texto al elemento: {}", locator, e);
            throw e;
        }
    }

    /**
     * Espera a que la pantalla de carga aparezca y desaparezca.
     */
    public void esperarPantallaCargaDesaparezca(String xpath) {
        By indicadorCarga = By.xpath(xpath);
        try {
            log.info("Esperando visibilidad del indicador de carga...");
            WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);

            wait.until(ExpectedConditions.visibilityOfElementLocated(indicadorCarga));
            log.info("✅ Pantalla de carga visible");

            wait.until(ExpectedConditions.invisibilityOfElementLocated(indicadorCarga));
            log.info("✅ Pantalla de carga desaparecida");
        } catch (TimeoutException e) {
            log.error("⚠️ La pantalla de carga no desapareció tras 2 minutos", e);
            throw e;
        }
    }

    // ---------------------------
    // Bloque: Gestos y movimientos
    // ---------------------------

    /**
     * Desplazarse hasta que el elemento esté visible (scroll).
     */
    public void swipeUp() {
        esperarPantallaEstable();
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.90);
        int endY = (int) (size.getHeight() * 0.50);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), x, startY))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(500),
                        PointerInput.Origin.viewport(), x, endY))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }

    public void swipeDown() {
        esperarPantallaEstable();
        Dimension size = driver.manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * 0.10);
        int endY = (int) (size.getHeight() * 0.90);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), x, startY))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(500),
                        PointerInput.Origin.viewport(), x, endY))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));

        hacerCapturaPantalla();
    }

    public void swipeLeft() {
        esperarPantallaEstable();
        Dimension size = driver.manage().window().getSize();
        int y = size.getHeight() / 2;
        int startX = (int) (size.getWidth() * 0.90);
        int endX = (int) (size.getWidth() * 0.10);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), startX, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(500),
                        PointerInput.Origin.viewport(), endX, y))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }

    public void swipeRight() {
        esperarPantallaEstable();
        Dimension size = driver.manage().window().getSize();
        int y = size.getHeight() / 2;
        int startX = (int) (size.getWidth() * 0.10);
        int endX = (int) (size.getWidth() * 0.90);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), startX, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(500),
                        PointerInput.Origin.viewport(), endX, y))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));

        hacerCapturaPantalla();
    }

    /**
     * Hace scroll up repetido hasta que el elemento sea visible o se agote el número de intentos.
     */
    public WebElement scrollToElement(By locator, int maxSwipes) {
        int attempts = 0;
        while (attempts < maxSwipes) {
            try {
                WebElement el = driver.findElement(locator);
                if (el.isDisplayed()) {
                    return el;
                }
            } catch (Exception ignored) {
                log.error("Error al hacer scroll al elemento: " + ignored.getMessage());
            }

            swipeUp();
            attempts++;
        }
        // al final, lanza excepción si no se encontró
        throw new NoSuchElementException("No se encontró el elemento tras " + maxSwipes + " swipes: " + locator);
    }

    public void swipeAtElement(By locator,
                               Direction direction,
                               int times,
                               double percent,          // 0.0 - 1.0 del alto/ancho del elemento
                               int durationMs) {        // duración del gesto

        esperarPantallaEstable();
        WebElement el = esperarVisible(locator);
        Rectangle r = el.getRect();

        // Centro del elemento
        int cx = r.getX() + r.getWidth() / 2;
        int cy = r.getY() + r.getHeight() / 2;

        // Distancia de swipe en función del tamaño del elemento
        int dy = (int) Math.max(1, r.getHeight() * percent);
        int dx = (int) Math.max(1, r.getWidth() * percent);

        for (int i = 0; i < times; i++) {
            int startX = cx, startY = cy, endX = cx, endY = cy;

            switch (direction) {
                case UP:
                    endY = cy - dy;
                    break;
                case DOWN:
                    endY = cy + dy;
                    break;
                case LEFT:
                    endX = cx - dx;
                    break;
                case RIGHT:
                    endX = cx + dx;
                    break;
            }

            Point start = clampToViewport(startX, startY);
            Point end = clampToViewport(endX, endY);

            performSwipe(start, end, durationMs);
            log.info("Swipe {} en área de {} ({} -> {})", direction, locator, start, end);
        }
    }

    // Ejecuta el swipe con W3C Actions
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

    // Ajusta coordenadas a los límites de la pantalla
    private Point clampToViewport(int x, int y) {
        Dimension size = driver.manage().window().getSize();
        int safeX = Math.max(5, Math.min(size.getWidth() - 5, x));
        int safeY = Math.max(5, Math.min(size.getHeight() - 5, y));
        return new Point(safeX, safeY);
    }

    // Enum para direcciones
    public enum Direction {UP, DOWN, LEFT, RIGHT}

    // ---------------------------
    // Bloque: Capturas y debugging
    // ---------------------------

    public String hacerCapturaPantalla() {
        esperarPantallaEstable();
        if (driver == null) {
            log.warn("⚠️ El driver es nulo. No se puede hacer la captura.");
            return null;
        }

        try {
            // Espera rápida a que la pantalla esté lista
            new FastUiSettle(driver).waitBriefly();
            // Captura en bytes
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            // Ruta base desde variable o /tmp
            // - En AWS DF lo recogerá el artifact si así se configura
            // - En BrowserStack/local: se guardará en /tmp (o el path proporcionado)
            String baseDir = System.getenv("DEVICEFARM_SCREENSHOT_PATH");
            if (baseDir == null || baseDir.isEmpty()) {
                baseDir = "/tmp";
            }

            // Nombre de archivo con timestamp
            String filename = "screenshot_" + Instant.now().toEpochMilli() + ".png";

            // Construye el Path y escribe el archivo
            Path filePath = Path.of(baseDir, filename);
            Files.createDirectories(filePath.getParent()); // crea el directorio si no existe
            Files.write(filePath, screenshot);

            String fullPath = filePath.toAbsolutePath().toString();
            log.info("📸 Captura de pantalla guardada en: {}", fullPath);
            return fullPath;

        } catch (IOException e) {
            log.error("💥 Error al guardar la captura de pantalla", e);
            return null;
        } catch (Exception e) {
            log.error("💥 Error inesperado realizando la captura de pantalla", e);
            return null;
        }
    }
}
