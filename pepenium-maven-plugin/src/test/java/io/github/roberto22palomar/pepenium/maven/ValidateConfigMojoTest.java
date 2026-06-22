package io.github.roberto22palomar.pepenium.maven;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidateConfigMojoTest {

    @TempDir
    Path tempDir;

    @Test
    void validatesAConsumerConfiguration() throws Exception {
        Path config = write("schemaVersion: 1\nprofiles:\n  local-web:\n    baseUrl: https://example.com\n");
        ValidateConfigMojo mojo = mojo(config);
        mojo.setProfileId("local-web");

        assertDoesNotThrow(mojo::execute);
    }

    @Test
    void convertsConfigurationErrorsIntoBuildFailures() throws Exception {
        ValidateConfigMojo mojo = mojo(write("schemaVersion: 2\n"));

        MojoFailureException error = assertThrows(MojoFailureException.class, mojo::execute);

        assertTrue(error.getMessage().contains("schemaVersion"));
    }

    @Test
    void supportsExplicitOptOut() {
        ValidateConfigMojo mojo = new ValidateConfigMojo();
        mojo.setSkip(true);

        assertDoesNotThrow(mojo::execute);
    }

    private ValidateConfigMojo mojo(Path config) {
        ValidateConfigMojo mojo = new ValidateConfigMojo();
        mojo.setConfigFile(config.toFile());
        return mojo;
    }

    private Path write(String content) throws Exception {
        Path config = tempDir.resolve("pepenium.yml");
        Files.writeString(config, content);
        return config;
    }
}
