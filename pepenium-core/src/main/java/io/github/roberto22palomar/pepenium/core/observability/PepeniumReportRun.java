package io.github.roberto22palomar.pepenium.core.observability;

import java.time.Instant;

final class PepeniumReportRun {

    private static final Instant STARTED_AT = Instant.now();
    private static final String ID = "run-" + STARTED_AT.toString().replaceAll("[^a-zA-Z0-9._-]+", "_");

    private PepeniumReportRun() {
    }

    static String id() {
        return ID;
    }

    static Instant startedAt() {
        return STARTED_AT;
    }

    static String indexFileName() {
        return "index-" + ID + ".html";
    }
}
