package io.github.roberto22palomar.pepenium.core.observability;

import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PepeniumBannerTest {

    @Test
    void doesNotPrintProviderCredentials() throws Exception {
        DriverRequest request = DriverRequest.builder()
                .driverType(DriverType.REMOTE_WEB)
                .serverUrl(new URL("https://user:access-secret@hub.example.test/wd/hub"))
                .description("remote web")
                .build();
        PrintStream previous = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.name()));
            PepeniumBanner.print(request);
        } finally {
            System.setOut(previous);
        }

        String banner = output.toString(StandardCharsets.UTF_8.name());
        assertTrue(banner.contains("https://hub.example.test/wd/hub"));
        assertFalse(banner.contains("user"));
        assertFalse(banner.contains("access-secret"));
    }
}
