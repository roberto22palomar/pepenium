package io.github.roberto22palomar.pepenium.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PepeniumConfigCliTest {

    @TempDir
    Path tempDir;

    @Test
    void validatesASelectedProfileWithoutStartingADriver() throws Exception {
        Path config = tempDir.resolve("pepenium.yml");
        Files.writeString(config, "schemaVersion: 1\nprofiles:\n  local-web:\n    baseUrl: https://example.com\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int exitCode = PepeniumConfigCli.run(new String[]{
                "--config", config.toString(), "--profile", "local-web"
        }, printStream(output), printStream(new ByteArrayOutputStream()));

        assertEquals(0, exitCode);
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Valid Pepenium configuration"));
    }

    @Test
    void reportsInvalidConfigurationWithAStableExitCode() throws Exception {
        Path config = tempDir.resolve("pepenium.yml");
        Files.writeString(config, "schemaVersion: 99\n");
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        int exitCode = PepeniumConfigCli.run(new String[]{"--config", config.toString()},
                printStream(new ByteArrayOutputStream()), printStream(error));

        assertEquals(2, exitCode);
        assertTrue(error.toString(StandardCharsets.UTF_8).contains("schemaVersion"));
    }

    @Test
    void resolvesSelectedProfilePlaceholdersDuringPreflight() throws Exception {
        Path config = tempDir.resolve("pepenium.yml");
        Files.writeString(config, "schemaVersion: 1\nprofiles:\n  local-web:\n"
                + "    baseUrl: ${PEPENIUM_TEST_VARIABLE_THAT_DOES_NOT_EXIST}\n");
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        int exitCode = PepeniumConfigCli.run(new String[]{
                "--config", config.toString(), "--profile", "local-web"
        }, printStream(new ByteArrayOutputStream()), printStream(error));

        assertEquals(2, exitCode);
        assertTrue(error.toString(StandardCharsets.UTF_8).contains(
                "PEPENIUM_TEST_VARIABLE_THAT_DOES_NOT_EXIST"));
    }

    @Test
    void rejectsUnknownArguments() {
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        int exitCode = PepeniumConfigCli.run(new String[]{"--unknown"},
                printStream(new ByteArrayOutputStream()), printStream(error));

        assertEquals(2, exitCode);
        assertTrue(error.toString(StandardCharsets.UTF_8).contains("Unknown argument"));
    }

    private PrintStream printStream(ByteArrayOutputStream output) {
        return new PrintStream(output, true, StandardCharsets.UTF_8);
    }
}
