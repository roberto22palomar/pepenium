package io.github.roberto22palomar.pepenium.core.config.validation;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebCapabilityOverridesTest {

    @Test
    void applyChromeSupportsHeadlessArgsAndGenericCapabilities() {
        ChromeOptions options = new ChromeOptions();

        WebCapabilityOverrides.applyChrome(Map.of(
                "PEPENIUM_WEB_HEADLESS", "true",
                "PEPENIUM_WEB_ACCEPT_INSECURE_CERTS", "true",
                "PEPENIUM_WEB_PAGE_LOAD_STRATEGY", "eager",
                "PEPENIUM_WEB_ARGS", "--incognito;--window-size=1920,1080",
                "PEPENIUM_WEB_CAPABILITIES", "custom:flag=true;custom:retries=3;custom:ratio=1.5"
        )::get, options);

        @SuppressWarnings("unchecked")
        Map<String, Object> chromeOptions = (Map<String, Object>) options.getCapability("goog:chromeOptions");
        assertTrue(((java.util.List<String>) chromeOptions.get("args")).contains("--headless=new"));
        assertTrue(((java.util.List<String>) chromeOptions.get("args")).contains("--incognito"));
        assertTrue(((java.util.List<String>) chromeOptions.get("args")).contains("--window-size=1920,1080"));
        assertEquals(Boolean.TRUE, options.getCapability("acceptInsecureCerts"));
        assertEquals(PageLoadStrategy.EAGER, options.getCapability("pageLoadStrategy"));
        assertEquals(Boolean.TRUE, options.getCapability("custom:flag"));
        assertEquals(3L, options.getCapability("custom:retries"));
        assertEquals(1.5d, options.getCapability("custom:ratio"));
    }

    @Test
    void applyFirefoxSupportsBinaryAndHeadless() {
        FirefoxOptions options = new FirefoxOptions();

        WebCapabilityOverrides.applyFirefox(Map.of(
                "PEPENIUM_WEB_HEADLESS", "true",
                "PEPENIUM_WEB_BINARY_PATH", "C:\\firefox\\firefox.exe"
        )::get, options);

        @SuppressWarnings("unchecked")
        Map<String, Object> firefoxOptions = (Map<String, Object>) options.getCapability("moz:firefoxOptions");
        assertTrue(((java.util.List<String>) firefoxOptions.get("args")).contains("-headless"));
        assertEquals("C:\\firefox\\firefox.exe", firefoxOptions.get("binary"));
    }

    @Test
    void applyEdgeSupportsArgsAndBrowserVersion() {
        EdgeOptions options = new EdgeOptions();

        WebCapabilityOverrides.applyEdge(Map.of(
                "PEPENIUM_WEB_ARGS", "--inprivate",
                "PEPENIUM_WEB_BROWSER_VERSION", "135"
        )::get, options);

        @SuppressWarnings("unchecked")
        Map<String, Object> edgeOptions = (Map<String, Object>) options.getCapability("ms:edgeOptions");
        assertTrue(((java.util.List<String>) edgeOptions.get("args")).contains("--inprivate"));
        assertEquals("135", options.getBrowserVersion());
    }

    @Test
    void applyThrowsOnInvalidBoolean() {
        ChromeOptions options = new ChromeOptions();

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> WebCapabilityOverrides.applyChrome(Map.of("PEPENIUM_WEB_HEADLESS", "maybe")::get, options));

        assertEquals("PEPENIUM_WEB_HEADLESS must be either 'true' or 'false'.", error.getMessage());
    }

    @Test
    void applyThrowsOnMalformedCapabilitiesList() {
        ChromeOptions options = new ChromeOptions();

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> WebCapabilityOverrides.applyChrome(
                        Map.of("PEPENIUM_WEB_CAPABILITIES", "bad-entry")::get,
                        options));

        assertEquals(
                "PEPENIUM_WEB_CAPABILITIES entries must follow key=value format and be separated with ';'.",
                error.getMessage());
    }
}
