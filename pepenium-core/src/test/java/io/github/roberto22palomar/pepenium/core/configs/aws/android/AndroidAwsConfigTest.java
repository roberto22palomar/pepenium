package io.github.roberto22palomar.pepenium.core.configs.aws.android;

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

class AndroidAwsConfigTest {

    @Test
    void createAndroidNativeLocalStartsOwnedService() throws Exception {
        AppiumServiceBuilder builder = mockBuilder();
        AppiumDriverLocalService service = mockService("http://127.0.0.1:49001");
        when(builder.build()).thenReturn(service);
        AndroidConfigAWS config = new AndroidConfigAWS(env(Map.of()), () -> builder);

        DriverRequest request = config.createRequest();

        assertEquals(DriverType.ANDROID_APPIUM, request.getDriverType());
        assertEquals("http://127.0.0.1:49001", request.getServerUrl().toString());
        assertEquals("AWS Android native app", request.getDescription());
        assertSame(service, request.getOwnedService());
        assertEquals("android", lowerCaseCapability(request, "platformName"));
        assertEquals("UiAutomator2", request.getCapabilities().getCapability("appium:automationName"));
        verify(service).start();
    }

    @Test
    void createAndroidNativeDeviceFarmRequestUsesProvidedEnvironment() throws Exception {
        AndroidConfigAWS config = new AndroidConfigAWS(
                env(Map.of(
                        "AWS_DEVICE_FARM", "1",
                        "DEVICEFARM_DEVICE_NAME", "Galaxy S24",
                        "DEVICEFARM_APP_PATH", "/tmp/devicefarm/app.apk"
                )),
                unexpectedBuilderFactory()
        );

        DriverRequest request = config.createRequest();

        assertEquals("http://127.0.0.1:4723/wd/hub", request.getServerUrl().toString());
        assertNull(request.getOwnedService());
        assertEquals("Galaxy S24", request.getCapabilities().getCapability("appium:deviceName"));
        assertEquals("/tmp/devicefarm/app.apk", request.getCapabilities().getCapability("appium:app"));
    }

    @Test
    void createAndroidNativeDeviceFarmRequestRejectsMissingAppPath() {
        AndroidConfigAWS config = new AndroidConfigAWS(
                env(Map.of(
                        "AWS_DEVICE_FARM", "1",
                        "DEVICEFARM_DEVICE_NAME", "Galaxy S24"
                )),
                unexpectedBuilderFactory()
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class, config::createRequest);

        assertTrue(exception.getMessage().contains("DEVICEFARM_APP_PATH"));
    }

    @Test
    void createAndroidWebLocalStartsOwnedServiceAndUsesChromeBrowser() throws Exception {
        AppiumServiceBuilder builder = mockBuilder();
        AppiumDriverLocalService service = mockService("http://127.0.0.1:49002");
        when(builder.build()).thenReturn(service);
        AndroidWebConfigAWS config = new AndroidWebConfigAWS(env(Map.of()), () -> builder);

        DriverRequest request = config.createRequest();

        assertEquals(DriverType.ANDROID_APPIUM, request.getDriverType());
        assertEquals("http://127.0.0.1:49002", request.getServerUrl().toString());
        assertSame(service, request.getOwnedService());
        assertEquals("Chrome", request.getCapabilities().getCapability("browserName"));
        verify(service).start();
    }

    @Test
    void createAndroidWebDeviceFarmRequestUsesFixedHubUrl() throws Exception {
        AndroidWebConfigAWS config = new AndroidWebConfigAWS(
                env(Map.of("DEVICEFARM_DEVICE_NAME", "Galaxy Web")),
                unexpectedBuilderFactory()
        );

        DriverRequest request = config.createRequest();

        assertEquals("http://127.0.0.1:4723", request.getServerUrl().toString());
        assertNull(request.getOwnedService());
        assertEquals("Galaxy Web", request.getCapabilities().getCapability("appium:deviceName"));
        assertEquals("Chrome", request.getCapabilities().getCapability("browserName"));
    }

    @Test
    void createAndroidWebDeviceFarmRequestRejectsMissingDeviceName() {
        AndroidWebConfigAWS config = new AndroidWebConfigAWS(
                env(Map.of("AWS_DEVICE_FARM", "1")),
                unexpectedBuilderFactory()
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class, config::createRequest);

        assertTrue(exception.getMessage().contains("DEVICEFARM_DEVICE_NAME"));
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
