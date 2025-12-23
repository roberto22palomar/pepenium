package io.github.roberto22palomar.pepenium.toolkit.utils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Slf4j
public class ActionsWeb {

    private final WebDriver driver;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(6L);
    private static final Duration LONG_TIMEOUT = Duration.ofSeconds(120L);

    // ====== Helpers anti-overlay y click robusto ======

    // En Actions.java
    private final By overlayAbierto = By.cssSelector("[data-slot='sheet-overlay'][data-state='open']");
    private final By btnCerrarSheet = By.cssSelector("[data-slot='sheet-close'], [data-state='open'] [aria-label='Close'], button[data-slot='sheet-close']");

    public void esperarOverlayAbierto() {
        new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.visibilityOfElementLocated(overlayAbierto));
    }

    public void cerrarSheetSiAbierto() {
        List<WebElement> abiertos = driver.findElements(overlayAbierto);
        if (!abiertos.isEmpty()) {
            // intenta cerrar con botón; si tu UI no tiene botón visible, usa ESC como fallback
            try {
                hacerClick(btnCerrarSheet);
            } catch (Exception ignore) {
                // Fallback: tecla ESC (usando la clase de Selenium con nombre completo)
                new org.openqa.selenium.interactions.Actions(driver)
                        .sendKeys(Keys.ESCAPE)
                        .perform();
            }
            new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(d -> d.findElements(overlayAbierto).isEmpty());
        }
    }

    /**
     * Espera hasta que haya al menos N elementos (para listas dinámicas de chips).
     */
    public void esperarNumeroDeElementosAlMenos(By locator, int n) {
        new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(d -> d.findElements(locator).size() >= n);
    }


    // ---------------------------
    // Bloque: Esperas y validaciones
    // ---------------------------

    /**
     * Espera hasta que el elemento esté visible en pantalla.
     */
    public WebElement esperarElementoVisible(By locator) {
        try {
            return new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            log.error("Timeout esperando visibilidad del elemento: {}", locator, e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado esperando visibilidad del elemento: {}", locator, e);
            throw e;
        }
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
            WebElement elemento = esperarElementoVisible(locator);
            return elemento.getText();
        } catch (Exception e) {
            log.error("Error obteniendo texto de elemento: {}", locator, e);
            return null;
        }
    }

    // ---------------------------
    // Bloque: Interacciones básicas
    // ---------------------------


    @SneakyThrows
/**
 * Espera hasta que el elemento sea clicable y hace clic.
 */
    public void hacerClick(By locator) {
        try {
            WebElement elemento = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(locator));
            elemento.click();
            Thread.sleep(2000);
            log.info("CLICK hecho en: {}", locator);
        } catch (TimeoutException e) {
            log.error("Timeout al hacer clic en el elemento: {}", locator, e);
            throw e;
        } catch (Exception e) {
            log.error("Error al hacer clic en el elemento: {}", locator, e);
            throw e;
        }
    }

    /**
     * Espera a que haya al menos un elemento para un locator y los devuelve
     */
    public List<WebElement> esperarYObtenerTodos(By locator) {
        new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, 0));
        return driver.findElements(locator);
    }

    /**
     * Cuenta elementos para un locator (esperando a que exista al menos uno)
     */
    public int contar(By locator) {
        return esperarYObtenerTodos(locator).size();
    }

    /**
     * Clic por índice dentro de una lista localizada por 'locator'
     */
    public void clickPorIndiceEnLista(By locator, int indice) {
        List<WebElement> elementos = esperarYObtenerTodos(locator);
        if (indice < 0 || indice >= elementos.size()) {
            throw new IllegalArgumentException(
                    "Índice fuera de rango: " + indice + " (size=" + elementos.size() + ")"
            );
        }
        WebElement objetivo = elementos.get(indice);
        clickConFallback(objetivo, locator, indice);
    }

    /**
     * Clic aleatorio dentro de una lista localizada por 'locator'
     */
    public void clickAleatorioEnLista(By locator) {
        List<WebElement> elementos = esperarYObtenerTodos(locator);
        int size = elementos.size();
        if (size == 0) {
            throw new NoSuchElementException("No hay elementos para: " + locator);
        }
        int indice = ThreadLocalRandom.current().nextInt(size);
        WebElement objetivo = elementos.get(indice);
        clickConFallback(objetivo, locator, indice);
    }


    /**
     * Clic con scroll y fallback JS por si el clic nativo falla (menús flotantes, overlays, etc.)
     */
    private void clickConFallback(WebElement elemento, By locator, int indice) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", elemento);
            new WebDriverWait(driver, DEFAULT_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(elemento));
            elemento.click();
            log.info("CLICK por índice {} en lista: {}", indice, locator);
        } catch (Exception e) {
            log.warn("Clic estándar falló, probando JS click. Motivo: {}", e.getMessage());
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
            log.info("CLICK por JS realizado en índice {} para: {}", indice, locator);
        }
    }


    /**
     * Espera visibilidad de un contenedor (útil para cuando se abre el menú)
     */
    public void esperarVisible(By locator) {
        log.info("<<< ESPERANDO QUE ESTÉ PRESENTE EL ELEMENTO: {}", locator);
        WebDriverWait wait = new WebDriverWait(driver, LONG_TIMEOUT);

        if (wait.until(ExpectedConditions.presenceOfElementLocated(locator)) != null) {
            log.info("<<< EL ELEMENTO ESTÁ PRESENTE: {}", locator);
        } else {
            log.warn("<<< EL ELEMENTO NO ESTÁ PRESENTE: {}", locator);
        }
    }

    /**
     * Hace clic si el elemento está visible.
     */
    public boolean hacerClickSiVisible(By locator) {
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
            WebElement elemento = esperarElementoVisible(locator);
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
    public void esperarPantallaCarga(String xpath) {
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

    /**
     * Hace scroll up repetido hasta que el elemento sea visible o se agote el número de intentos.
     */
    /**
     * Espera a que el elemento esté presente, visible y clickeable.
     * Si está fuera del viewport, hace scroll hasta él y luego hace click.
     */
    /**
     * Scrollea progresivamente hasta encontrar un elemento.
     * Cuando lo detecta en el DOM, lo centra en pantalla y hace click.
     */
    public void scrollHastaEncontrarYClick(By locator, int maxScrolls, int stepPx) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        for (int i = 0; i < maxScrolls; i++) {
            try {
                // 1️⃣ Busca el elemento en el DOM
                WebElement el = driver.findElement(locator);

                // 2️⃣ Espera que sea visible (por si React tarda en mostrarlo)
                wait.until(ExpectedConditions.visibilityOf(el));

                // 3️⃣ Centra en el viewport y hace click
                js.executeScript("arguments[0].scrollIntoView({block:'center', behavior:'instant'});", el);
                Thread.sleep(300);
                el.click();
                log.info("✅ Click hecho en {} tras {} scrolls", locator, i);
                return;

            } catch (NoSuchElementException e) {
                // ❌ No está aún → seguir bajando
                js.executeScript("window.scrollBy(0, arguments[0]);", stepPx);
                log.debug("🔽 Scroll {} (+{} px)", i + 1, stepPx);
                try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            } catch (TimeoutException e) {
                log.debug("⌛ Elemento hallado pero aún no visible (scroll {}), reintentando...", i + 1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        throw new NoSuchElementException("❌ No se encontró el elemento tras " + maxScrolls + " scrolls: " + locator);
    }




    // ---------------------------
    // Bloque: Capturas y debugging
    // ---------------------------

    /**
     * Captura una pantalla y la guarda en ruta configurable o /tmp.
     *
     * @return Ruta absoluta del archivo de captura.
     */
    public String hacerCapturaPantalla() {
        if (driver == null) {
            log.warn("⚠️ El driver es nulo. No se puede hacer la captura.");
            return null;
        }

        try {
            // Captura en bytes
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            // Ruta base desde variable o /tmp
            //Actualmente si se lanza en AWS se guardan en un artifact
            // Si es en BrowserStack se guarda en el PC que lance la Auto. en /tmp
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
            log.error("💥 Error al guardar la captura de pantalla en Device Farm", e);
            return null;
        }
    }

}
