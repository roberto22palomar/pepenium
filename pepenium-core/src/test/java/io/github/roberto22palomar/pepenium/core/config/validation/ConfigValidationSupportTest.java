package io.github.roberto22palomar.pepenium.core.config.validation;

import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfigDesktop;
import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfigMobile;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigValidationSupportTest {

    @Test
    void requireUrlRejectsInvalidUrlWithHelpfulMessage() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> ConfigValidationSupport.requireUrl("not-a-url", "APPIUM_URL", "hint")
        );

        assertTrue(error.getMessage().contains("APPIUM_URL must be a valid URL"));
    }

    @Test
    void requireExistingFileRejectsMissingFile() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> ConfigValidationSupport.requireExistingFile("C:/missing/app.apk", "APP_PATH", "hint")
        );

        assertTrue(error.getMessage().contains("APP_PATH points to a path that does not exist"));
    }

    @Test
    void validateBrowserStackAppConfigRejectsMissingCredentials() {
        BrowserStackConfig config = BrowserStackConfig.builder()
                .app("bs://app")
                .platforms(List.of(new BrowserStackConfig.Platform("Android", "Pixel", "14")))
                .build();

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> ConfigValidationSupport.validateBrowserStackAppConfig(config, "browserstack.yml")
        );

        assertTrue(error.getMessage().contains("BrowserStack userName is required"));
    }

    @Test
    void validateBrowserStackDesktopConfigRejectsIncompletePlatform() {
        BrowserStackConfigDesktop config = BrowserStackConfigDesktop.builder()
                .userName("user")
                .accessKey("key")
                .platforms(List.of(new BrowserStackConfigDesktop.Platform("OS X", "Sonoma", "Chrome", null)))
                .build();

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> ConfigValidationSupport.validateBrowserStackDesktopConfig(config, "browserstack.yml")
        );

        assertTrue(error.getMessage().contains("browserVersion"));
    }

    @Test
    void validateBrowserStackMobileWebConfigAcceptsValidConfig() {
        BrowserStackConfigMobile config = BrowserStackConfigMobile.builder()
                .userName("user")
                .accessKey("key")
                .platforms(List.of(new BrowserStackConfigMobile.Platform("Pixel 8", "14", "Chrome", "portrait")))
                .build();

        BrowserStackConfigMobile validated =
                ConfigValidationSupport.validateBrowserStackMobileWebConfig(config, "browserstack.yml");

        assertEquals("user", validated.getUserName());
    }
}
