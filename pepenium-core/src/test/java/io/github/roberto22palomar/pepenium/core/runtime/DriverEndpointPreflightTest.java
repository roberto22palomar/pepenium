package io.github.roberto22palomar.pepenium.core.runtime;

import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverEndpointPreflightTest {

    @Test
    void acceptsReachableLocalEndpoint() throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            DriverRequest request = request(new URL("http://127.0.0.1:" + server.getLocalPort()));

            assertDoesNotThrow(() -> DriverEndpointPreflight.verifyLocalEndpoint(request, Duration.ofSeconds(1)));
        }
    }

    @Test
    void rejectsUnavailableLocalEndpointWithActionableMessage() throws Exception {
        int unusedPort;
        try (ServerSocket server = new ServerSocket(0)) {
            unusedPort = server.getLocalPort();
        }
        DriverRequest request = request(new URL("http://127.0.0.1:" + unusedPort));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> DriverEndpointPreflight.verifyLocalEndpoint(request, Duration.ofMillis(200))
        );

        assertTrue(error.getMessage().contains("Start Appium"));
        assertTrue(error.getMessage().contains("127.0.0.1:" + unusedPort));
    }

    @Test
    void leavesRemoteCloudEndpointsToSeleniumTransport() throws Exception {
        DriverRequest request = request(new URL("https://hub-cloud.browserstack.com/wd/hub"));

        assertDoesNotThrow(() -> DriverEndpointPreflight.verifyLocalEndpoint(request, Duration.ofMillis(1)));
    }

    private static DriverRequest request(URL url) {
        return DriverRequest.builder()
                .driverType(DriverType.ANDROID_APPIUM)
                .description("local Android")
                .serverUrl(url)
                .build();
    }
}
