package io.github.roberto22palomar.pepenium.core.runtime;

import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.observability.SensitiveDataSanitizer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.time.Duration;

final class DriverEndpointPreflight {

    private DriverEndpointPreflight() {
    }

    static void verifyLocalEndpoint(DriverRequest request, Duration timeout) {
        URL serverUrl = request.getServerUrl();
        if (serverUrl == null || !isLoopback(serverUrl.getHost())) {
            return;
        }

        int port = serverUrl.getPort() >= 0 ? serverUrl.getPort() : serverUrl.getDefaultPort();
        if (port < 0) {
            return;
        }

        int timeoutMillis = (int) Math.min(Integer.MAX_VALUE, Math.max(1L, timeout.toMillis()));
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverUrl.getHost(), port), timeoutMillis);
        } catch (IOException error) {
            String safeUrl = SensitiveDataSanitizer.sanitizeServerUrl(serverUrl);
            throw new IllegalStateException(
                    "Cannot connect to local driver endpoint " + safeUrl + " within " + timeout
                            + ". Start Appium or correct the profile serverUrl before running the test.",
                    error
            );
        }
    }

    private static boolean isLoopback(String host) {
        return "localhost".equalsIgnoreCase(host)
                || "::1".equals(host)
                || host.startsWith("127.");
    }
}
