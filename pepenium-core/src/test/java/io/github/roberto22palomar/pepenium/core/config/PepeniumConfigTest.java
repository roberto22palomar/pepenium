package io.github.roberto22palomar.pepenium.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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
        assertEquals("custom:flag=true;custom:retries=3",
                resolved.value("local-web", "PEPENIUM_WEB_CAPABILITIES"));
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

    private Path writeConfig(String content) throws Exception {
        Path config = tempDir.resolve("pepenium.yml");
        Files.writeString(config, content);
        return config;
    }
}
