package io.github.roberto22palomar.pepenium.core.config;

import java.io.PrintStream;
import java.nio.file.Path;

/**
 * Command-line preflight for validating {@code pepenium.yml} without opening a driver session.
 */
public final class PepeniumConfigCli {

    private PepeniumConfigCli() {
    }

    public static void main(String[] args) {
        int exitCode = run(args, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args, PrintStream output, PrintStream error) {
        String configPath = "pepenium.yml";
        String profileId = null;
        try {
            for (int index = 0; index < args.length; index++) {
                switch (args[index]) {
                    case "--config":
                        configPath = requireValue(args, ++index, "--config");
                        break;
                    case "--profile":
                        profileId = requireValue(args, ++index, "--profile");
                        break;
                    case "--help":
                        output.println("Usage: PepeniumConfigCli [--config <path>] [--profile <id>]");
                        return 0;
                    default:
                        throw new IllegalArgumentException("Unknown argument: " + args[index]);
                }
            }
            Path path = Path.of(configPath);
            PepeniumConfig.validate(path, profileId);
            output.printf("Valid Pepenium configuration: %s%s%n",
                    path.toAbsolutePath(), profileId == null ? "" : " (profile " + profileId + ")");
            return 0;
        } catch (IllegalArgumentException | IllegalStateException errorCause) {
            error.println(errorCause.getMessage());
            return 2;
        }
    }

    private static String requireValue(String[] args, int index, String option) {
        if (index >= args.length || args[index].isBlank()) {
            throw new IllegalArgumentException("Missing value for " + option);
        }
        return args[index].trim();
    }
}
