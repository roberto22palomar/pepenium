package io.github.roberto22palomar.pepenium.maven;

import io.github.roberto22palomar.pepenium.core.config.PepeniumConfig;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitConfigMojoTest {

    @TempDir
    Path tempDir;

    @Test
    void createsAValidLocalWebStarter() throws Exception {
        Path output = tempDir.resolve("nested/pepenium.yml");
        InitConfigMojo mojo = mojo(output);

        assertDoesNotThrow(mojo::execute);

        String content = Files.readString(output);
        assertTrue(content.contains("schemaVersion: 1"));
        assertTrue(content.contains("defaultProfile: local-web"));
        assertTrue(content.contains("headless: true"));
        assertDoesNotThrow(() -> PepeniumConfig.validate(output, "local-web"));
    }

    @Test
    void createsProviderAwareTemplates() throws Exception {
        Path output = tempDir.resolve("browserstack.yml");
        InitConfigMojo mojo = mojo(output);
        mojo.setTemplate("browserstack-web");

        mojo.execute();

        String content = Files.readString(output);
        assertTrue(content.contains("defaultProfile: browserstack-windows-web"));
        assertTrue(content.contains("credentials and platform selection remain in its provider YAML"));
        assertDoesNotThrow(() -> PepeniumConfig.validate(output, "browserstack-windows-web"));
    }

    @Test
    void refusesToOverwriteExistingConfiguration() throws Exception {
        Path output = tempDir.resolve("pepenium.yml");
        Files.writeString(output, "keep-me");

        MojoFailureException error = assertThrows(MojoFailureException.class, () -> mojo(output).execute());

        assertTrue(error.getMessage().contains("already exists"));
        assertEquals("keep-me", Files.readString(output));
    }

    @Test
    void overwritesOnlyWhenForceIsExplicit() throws Exception {
        Path output = tempDir.resolve("pepenium.yml");
        Files.writeString(output, "replace-me");
        InitConfigMojo mojo = mojo(output);
        mojo.setTemplate("local-android");
        mojo.setForce(true);

        mojo.execute();

        assertTrue(Files.readString(output).contains("defaultProfile: local-android"));
    }

    @Test
    void listsSupportedTemplatesForInvalidInput() {
        InitConfigMojo mojo = mojo(tempDir.resolve("pepenium.yml"));
        mojo.setTemplate("unknown");

        MojoFailureException error = assertThrows(MojoFailureException.class, mojo::execute);

        assertTrue(error.getMessage().contains("local-web, local-android, browserstack-web"));
    }

    private InitConfigMojo mojo(Path output) {
        InitConfigMojo mojo = new InitConfigMojo();
        mojo.setConfigFile(output.toFile());
        return mojo;
    }
}
