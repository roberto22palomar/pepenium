package io.github.roberto22palomar.pepenium.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PepeniumConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void resolvesStructuredProfileValuesAndCollections() throws Exception {
        Path config = writeConfig("defaultProfile: local-web\n"
                + "reporting:\n"
                + "  directory: target/reports\n"
                + "timeouts:\n"
                + "  action: 2s\n"
                + "profiles:\n"
                + "  local-web:\n"
                + "    timeouts:\n"
                + "      action: 750ms\n"
                + "    browser:\n"
                + "      headless: true\n"
                + "      arguments: [--incognito, '--window-size=1280,720']\n"
                + "    capabilities:\n"
                + "      custom:flag: true\n"
                + "      custom:retries: 3\n");

        PepeniumConfig.ResolvedConfig resolved = PepeniumConfig.load(config, true, key -> null);

        assertEquals("local-web", resolved.defaultProfile());
        assertEquals("target/reports", resolved.value("local-web", "PEPENIUM_REPORT_DIR"));
        assertEquals("750ms", resolved.value("local-web", "PEPENIUM_ACTION_TIMEOUT_SECONDS"));
        assertEquals("2s", resolved.value("other-profile", "PEPENIUM_ACTION_TIMEOUT_SECONDS"));
        assertEquals("true", resolved.value("local-web", "PEPENIUM_WEB_HEADLESS"));
        assertEquals("--incognito;--window-size=1280,720",
                resolved.value("local-web", "PEPENIUM_WEB_ARGS"));
        assertEquals(
                Map.of("custom:flag", true, "custom:retries", 3),
                resolved.capabilities("local-web")
        );
    }

    @Test
    void resolvesEnvironmentPlaceholdersOnlyWhenSelected() throws Exception {
        Path config = writeConfig("profiles:\n"
                + "  local-android:\n"
                + "    app:\n"
                + "      path: ${APP_BINARY}\n");
        PepeniumConfig.ResolvedConfig resolved = PepeniumConfig.load(
                config,
                true,
                Map.of("APP_BINARY", "build/app.apk")::get
        );

        assertEquals("build/app.apk", resolved.value("local-android", "APP_PATH"));
    }

    @Test
    void reportsMissingSecretWithItsYamlPath() throws Exception {
        Path config = writeConfig("profiles:\n"
                + "  local-android:\n"
                + "    app:\n"
                + "      path: ${MISSING_APP}\n");
        PepeniumConfig.ResolvedConfig resolved = PepeniumConfig.load(config, true, key -> null);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> resolved.value("local-android", "APP_PATH")
        );

        assertTrue(error.getMessage().contains("MISSING_APP"));
        assertTrue(error.getMessage().contains("profiles.local-android.app.path"));
    }

    @Test
    void rejectsMissingExplicitConfigFile() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> PepeniumConfig.load(tempDir.resolve("missing.yml"), true, key -> null)
        );

        assertTrue(error.getMessage().contains("Configuration file does not exist"));
    }

    @Test
    void preservesAndDeepMergesStructuredCapabilities() throws Exception {
        Path config = writeConfig("defaultProfile: local-web\n"
                + "capabilities:\n"
                + "  vendor:options:\n"
                + "    project: Pepenium\n"
                + "    retries: 2\n"
                + "  acceptInsecureCerts: false\n"
                + "profiles:\n"
                + "  local-web:\n"
                + "    capabilities:\n"
                + "      vendor:options:\n"
                + "        retries: 4\n"
                + "        tags: [smoke, '${RUN_TAG}']\n"
                + "      acceptInsecureCerts: true\n");

        PepeniumConfig.ResolvedConfig resolved = PepeniumConfig.load(
                config,
                true,
                Map.of("RUN_TAG", "chrome")::get
        );

        Map<String, Object> capabilities = resolved.capabilities("local-web");
        assertEquals(true, capabilities.get("acceptInsecureCerts"));
        @SuppressWarnings("unchecked")
        Map<String, Object> vendorOptions = (Map<String, Object>) capabilities.get("vendor:options");
        assertEquals("Pepenium", vendorOptions.get("project"));
        assertEquals(4, vendorOptions.get("retries"));
        assertEquals(List.of("smoke", "chrome"), vendorOptions.get("tags"));
    }

    @Test
    void rejectsUnknownKeysWithTheirYamlPath() throws Exception {
        Path config = writeConfig("profiles:\n"
                + "  local-web:\n"
                + "    browser:\n"
                + "      typoHeadles: true\n");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> PepeniumConfig.load(config, true, key -> null)
        );

        assertTrue(error.getMessage().contains("profiles.local-web.browser.typoHeadles"));
    }

    @Test
    void rejectsCapabilitiesThatAreNotObjects() throws Exception {
        Path config = writeConfig("profiles:\n"
                + "  local-web:\n"
                + "    capabilities: [invalid]\n");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> PepeniumConfig.load(config, true, key -> null)
        );

        assertTrue(error.getMessage().contains("profiles.local-web.capabilities"));
        assertTrue(error.getMessage().contains("must be a YAML object"));
    }

    @Test
    void rejectsUnsupportedSchemaVersions() throws Exception {
        Path config = writeConfig("schemaVersion: 2\nprofiles: {}\n");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> PepeniumConfig.load(config, true, key -> null)
        );

        assertTrue(error.getMessage().contains("schemaVersion"));
        assertTrue(error.getMessage().contains("integer 1"));
    }

    @Test
    void rejectsInvalidUrlsAndTimeoutsAtLoadTime() throws Exception {
        Path invalidUrl = writeConfig("baseUrl: ftp://example.com\nprofiles: {}\n");
        IllegalStateException urlError = assertThrows(
                IllegalStateException.class,
                () -> PepeniumConfig.load(invalidUrl, true, key -> null)
        );
        assertTrue(urlError.getMessage().contains("baseUrl"));

        Path invalidTimeout = writeConfig("timeouts:\n  action: eventually\nprofiles: {}\n");
        IllegalStateException timeoutError = assertThrows(
                IllegalStateException.class,
                () -> PepeniumConfig.load(invalidTimeout, true, key -> null)
        );
        assertTrue(timeoutError.getMessage().contains("timeouts.action"));
    }

    @Test
    void rejectsAmbiguousBrowserTypesAtLoadTime() throws Exception {
        Path config = writeConfig("profiles:\n"
                + "  local-web:\n"
                + "    browser:\n"
                + "      headless: 'yes'\n"
                + "      arguments: --incognito\n");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> PepeniumConfig.load(config, true, key -> null)
        );

        assertTrue(error.getMessage().contains("profiles.local-web.browser.headless"));
    }

    @Test
    void rejectsProviderOwnedSettingsInPepeniumYaml() throws Exception {
        Path config = writeConfig("profiles:\n"
                + "  browserstack-android:\n"
                + "    serverUrl: https://hub-cloud.browserstack.com/wd/hub\n");
        PepeniumConfig.ResolvedConfig resolved = PepeniumConfig.load(config, true, key -> null);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> resolved.validateProfile("browserstack-android")
        );

        assertTrue(error.getMessage().contains("provider-owned"));
        assertTrue(error.getMessage().contains("existing YAML"));
    }

    @Test
    void rejectsBrowserStackOwnedCapabilityNamespaces() throws Exception {
        Path config = writeConfig("capabilities:\n"
                + "  bstack:options:\n"
                + "    projectName: Pepenium\n"
                + "profiles:\n"
                + "  browserstack-android: {}\n");
        PepeniumConfig.ResolvedConfig resolved = PepeniumConfig.load(config, true, key -> null);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> resolved.validateProfile("browserstack-android")
        );

        assertTrue(error.getMessage().contains("bstack:options"));
        assertTrue(error.getMessage().contains("BrowserStack YAML"));
    }

    private Path writeConfig(String content) throws Exception {
        Path config = tempDir.resolve("pepenium.yml");
        Files.writeString(config, content);
        return config;
    }
}
