package io.github.roberto22palomar.pepenium.toolkit.yaml;

import io.github.roberto22palomar.pepenium.toolkit.browserstack.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.toolkit.browserstack.BrowserStackConfigDesktop;
import io.github.roberto22palomar.pepenium.toolkit.browserstack.BrowserStackConfigMobile;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlLoaderTest {

    @Test
    void resolvesBrowserstackExampleDirectoryWhenPrimaryPathIsMissing() {
        Path resolvedPath = YamlLoader.resolvePath("src/test/resources/browserstackIOS.yml");

        assertEquals(
                Path.of("src", "test", "resources", "browserstackExamples", "browserstackIOS.yml.example"),
                resolvedPath
        );
    }

    @Test
    void loadsDesktopExampleConfigFromFallbackExampleFile() {
        BrowserStackConfigDesktop config = YamlLoaderDesktop.load("src/test/resources/browserstack.yml");

        assertEquals("user", config.getUserName());
        assertEquals("BrowserStack Sample", config.getProjectName());
    }

    @Test
    void loadsMobileExampleConfigFromFallbackExampleFile() {
        BrowserStackConfigMobile config = YamlLoaderMobile.load("src/test/resources/browserstackIOSWEB.yml");

        assertEquals("YOUR_BROWSERSTACK_USERNAME", config.getUserName());
        assertEquals("Pepenium-iOS-Web", config.getProjectName());
    }

    @Test
    void loadsAppExampleConfigFromFallbackExampleFile() {
        BrowserStackConfig config = YamlLoader.load("src/test/resources/browserstackIOS.yml");

        assertEquals("YOUR_BROWSERSTACK_USERNAME", config.getUserName());
        assertEquals("Pepenium-iOS", config.getProjectName());
    }

    @Test
    void throwsHelpfulErrorWhenNoCandidatePathExists() {
        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> YamlLoader.resolvePath("src/test/resources/does-not-exist.yml")
        );

        assertTrue(error.getMessage().contains("Could not find BrowserStack YAML"));
    }
}
