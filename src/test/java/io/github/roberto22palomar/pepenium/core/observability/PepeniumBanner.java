package io.github.roberto22palomar.pepenium.core.observability;

import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PepeniumBanner {

    private static final String[] ART = {
            "  ____                          _                 ",
            " |  _ \\ ___ _ __   ___ _ __   (_)_   _ _ __ ___  ",
            " | |_) / _ \\ '_ \\ / _ \\ '_ \\  | | | | | '_ ` _ \\ ",
            " |  __/  __/ |_) |  __/ | | | | | |_| | | | | | |",
            " |_|   \\___| .__/ \\___|_| |_| |_|\\__,_|_| |_| |_|",
            "           |_|                                    "
    };

    private PepeniumBanner() {
    }

    public static void print(DriverRequest request) {
        String banner = buildBanner(request);
        System.out.println();
        System.out.println(banner);
        log.info("Pepenium session started: {} [{}]", safe(request.getDescription()), request.getDriverType());
    }

    private static String buildBanner(DriverRequest request) {
        String lineSeparator = System.lineSeparator();
        StringBuilder builder = new StringBuilder();

        for (String line : ART) {
            builder.append(line).append(lineSeparator);
        }

        builder.append(" :: Pepenium :: ")
                .append(safe(request.getDescription()))
                .append(lineSeparator)
                .append(" :: Driver    :: ")
                .append(request.getDriverType())
                .append(lineSeparator);

        if (request.getServerUrl() != null) {
            builder.append(" :: Server    :: ")
                    .append(request.getServerUrl())
                    .append(lineSeparator);
        }

        return builder.toString().trim();
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "Starting session" : value.trim();
    }
}
