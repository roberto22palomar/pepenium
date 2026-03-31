package io.github.roberto22palomar.pepenium.core.config.yaml;

import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfigDesktop;
import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfigMobile;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlLoaderTest {

    @Test
    void resolvesBrowserstackExampleDirectoryWhenPrimaryPathIsMissing() {
        Path resolvedPath = YamlLoader.resolvePath("src/test/resources/browserstackIOS.yml");

        assertTrue(resolvedPath.endsWith(Path.of("src", "main", "resources", "browserstackExamples", "browserstackIOS.yml.example")));
    }

    @Test
    void resolvesLocalPepeniumBrowserstackDirectoryBeforeExamples() throws Exception {
        Path localDir = Path.of(".pepenium", "browserstack");
        Files.createDirectories(localDir);
        Path yaml = localDir.resolve("browserstack-local.yml");
        Files.writeString(yaml, "userName: user\naccessKey: key\nframework: junit5\nplatforms:\n  - os: Windows\n    osVersion: \"11\"\n    browserName: Chrome\n    browserVersion: latest\nparallelsPerPlatform: 1\nbrowserstackLocal: false\nbuildName: local-build\nprojectName: Local BrowserStack\n");

        Path resolvedPath = YamlLoader.resolvePath("browserstack-local.yml");

        assertEquals(yaml.normalize(), resolvedPath.normalize());
        Files.deleteIfExists(yaml);
        Files.deleteIfExists(localDir);
        Files.deleteIfExists(localDir.getParent());
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
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> YamlLoader.resolvePath("src/test/resources/does-not-exist.yml")
        );

        assertTrue(error.getMessage().contains("Could not find BrowserStack YAML"));
        assertTrue(error.getMessage().contains(".pepenium/browserstack"));
    }

    @Test
    void rejectsRuntimeResourcePathForRealBrowserStackYaml() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> YamlLoader.resolvePath("src/main/resources/browserstack.yml")
        );

        assertTrue(error.getMessage().contains("must live outside 'src/main/resources'"));
    }

    @Test
    void rejectsInvalidBrowserStackYamlContentClearly() throws Exception {
        Path invalidYaml = java.nio.file.Files.createTempFile("browserstack-invalid", ".yml");
        java.nio.file.Files.writeString(invalidYaml, "userName: user\naccessKey: key\nplatforms: []\n");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> YamlLoader.load(invalidYaml.toString())
        );

        assertTrue(error.getMessage().contains("BrowserStack app is required"));
    }
}
