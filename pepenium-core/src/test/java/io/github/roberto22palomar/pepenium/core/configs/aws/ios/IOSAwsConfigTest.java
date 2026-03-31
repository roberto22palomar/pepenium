package io.github.roberto22palomar.pepenium.core.configs.aws.ios;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IOSAwsConfigTest {

    @Test
    void createIosLocalStartsOwnedServiceWithDefaults() throws Exception {
        AppiumServiceBuilder builder = mockBuilder();
        AppiumDriverLocalService service = mockService("http://127.0.0.1:49003");
        when(builder.build()).thenReturn(service);
        IOSConfigAWS config = new IOSConfigAWS(env(Map.of()), () -> builder);

        DriverRequest request = config.createRequest();

        assertEquals(DriverType.IOS_APPIUM, request.getDriverType());
        assertEquals("http://127.0.0.1:49003", request.getServerUrl().toString());
        assertEquals("AWS iOS native app", request.getDescription());
        assertSame(service, request.getOwnedService());
        assertEquals("ios", lowerCaseCapability(request, "platformName"));
        assertEquals("XCUITest", request.getCapabilities().getCapability("appium:automationName"));
        assertEquals("iPhone Simulator", request.getCapabilities().getCapability("appium:deviceName"));
        verify(service).start();
    }

    @Test
    void createIosDeviceFarmRequestUsesIosAppPathFallback() throws Exception {
        IOSConfigAWS config = new IOSConfigAWS(
                env(Map.of(
                        "AWS_DEVICE_FARM", "1",
                        "DEVICEFARM_DEVICE_NAME", "iPhone 15",
                        "IOS_APP_PATH", "/tmp/devicefarm/app.zip"
                )),
                unexpectedBuilderFactory()
        );

        DriverRequest request = config.createRequest();

        assertEquals("http://127.0.0.1:4723/wd/hub", request.getServerUrl().toString());
        assertNull(request.getOwnedService());
        assertEquals("iPhone 15", request.getCapabilities().getCapability("appium:deviceName"));
        assertEquals("/tmp/devicefarm/app.zip", request.getCapabilities().getCapability("appium:app"));
        assertEquals(Boolean.TRUE, request.getCapabilities().getCapability("appium:autoAcceptAlerts"));
    }

    @Test
    void createIosDeviceFarmRequestRejectsMissingAppPath() {
        IOSConfigAWS config = new IOSConfigAWS(
                env(Map.of(
                        "AWS_DEVICE_FARM", "1",
                        "DEVICEFARM_DEVICE_NAME", "iPhone 15"
                )),
                unexpectedBuilderFactory()
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class, config::createRequest);

        assertTrue(exception.getMessage().contains("DEVICEFARM_APP_PATH / IOS_APP_PATH"));
    }

    private static AppiumServiceBuilder mockBuilder() {
        AppiumServiceBuilder builder = mock(AppiumServiceBuilder.class);
        when(builder.usingAnyFreePort()).thenReturn(builder);
        when(builder.withArgument(any(), eq("chromedriver_autodownload"))).thenReturn(builder);
        return builder;
    }

    private static AppiumDriverLocalService mockService(String url) throws Exception {
        AppiumDriverLocalService service = mock(AppiumDriverLocalService.class);
        when(service.getUrl()).thenReturn(new URL(url));
        return service;
    }

    private static Function<String, String> env(Map<String, String> values) {
        return values::get;
    }

    private static Supplier<AppiumServiceBuilder> unexpectedBuilderFactory() {
        return () -> {
            throw new AssertionError("AppiumServiceBuilder should not be used for Device Farm requests.");
        };
    }

    private static String lowerCaseCapability(DriverRequest request, String capabilityName) {
        Object value = request.getCapabilities().getCapability(capabilityName);
        return value == null ? null : String.valueOf(value).toLowerCase();
    }
}
