package io.github.roberto22palomar.pepenium.core.configs.local.android;

import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AndroidLocalConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void createAndroidNativeRequestUsesAppPathAndDeviceDefaults() throws Exception {
        Path apk = Files.createFile(tempDir.resolve("app.apk"));
        AndroidConfigLocal config = new AndroidConfigLocal(env(Map.of(
                "APPIUM_URL", "http://127.0.0.1:4725",
                "ANDROID_UDID", "emulator-5556",
                "ANDROID_DEVICE_NAME", "Pixel Local",
                "APP_PATH", "\"" + apk + "\""
        )));

        DriverRequest request = config.createRequest();

        assertEquals(DriverType.ANDROID_APPIUM, request.getDriverType());
        assertEquals("http://127.0.0.1:4725", request.getServerUrl().toString());
        assertEquals("Local Android native app", request.getDescription());
        assertEquals("android", lowerCaseCapability(request, "platformName"));
        assertEquals("UiAutomator2", request.getCapabilities().getCapability("appium:automationName"));
        assertEquals("Pixel Local", request.getCapabilities().getCapability("appium:deviceName"));
        assertEquals("emulator-5556", request.getCapabilities().getCapability("appium:udid"));
        assertEquals(apk.toString(), request.getCapabilities().getCapability("appium:app"));
        assertEquals(Boolean.TRUE, request.getCapabilities().getCapability("appium:autoGrantPermissions"));
        assertEquals(Boolean.FALSE, request.getCapabilities().getCapability("appium:noReset"));
        assertEquals(Boolean.FALSE, request.getCapabilities().getCapability("appium:fullReset"));
    }

    @Test
    void createAndroidNativeRequestSupportsPackageAndActivityConfiguration() throws Exception {
        AndroidConfigLocal config = new AndroidConfigLocal(env(Map.of(
                "APP_PACKAGE", "io.github.example",
                "APP_ACTIVITY", ".MainActivity"
        )));

        DriverRequest request = config.createRequest();

        assertEquals("io.github.example", request.getCapabilities().getCapability("appium:appPackage"));
        assertEquals(".MainActivity", request.getCapabilities().getCapability("appium:appActivity"));
        assertEquals("http://localhost:4723", request.getServerUrl().toString());
    }

    @Test
    void createAndroidNativeRequestRejectsMissingAppConfiguration() {
        AndroidConfigLocal config = new AndroidConfigLocal(env(Map.of()));

        IllegalStateException exception = assertThrows(IllegalStateException.class, config::createRequest);

        assertTrue(exception.getMessage().contains("Provide APP_PATH, or provide both APP_PACKAGE and APP_ACTIVITY."));
    }

    @Test
    void createAndroidNativeRequestRejectsPartialPackageActivityConfiguration() {
        AndroidConfigLocal config = new AndroidConfigLocal(env(Map.of(
                "APP_PACKAGE", "io.github.example"
        )));

        IllegalStateException exception = assertThrows(IllegalStateException.class, config::createRequest);

        assertTrue(exception.getMessage().contains("APP_PACKAGE and APP_ACTIVITY must be provided together."));
    }

    @Test
    void createAndroidWebRequestUsesDefaultsAndChromeBrowser() throws Exception {
        AndroidWebConfigLocal config = new AndroidWebConfigLocal(env(Map.of()));

        DriverRequest request = config.createRequest();

        assertEquals(DriverType.ANDROID_APPIUM, request.getDriverType());
        assertEquals("http://localhost:4723", request.getServerUrl().toString());
        assertEquals("Local Android mobile web", request.getDescription());
        assertEquals("android", lowerCaseCapability(request, "platformName"));
        assertEquals("Android Device", request.getCapabilities().getCapability("appium:deviceName"));
        assertEquals("emulator-5554", request.getCapabilities().getCapability("appium:udid"));
        assertEquals("Chrome", request.getCapabilities().getCapability("browserName"));
    }

    @Test
    void createAndroidWebRequestRejectsInvalidAppiumUrl() {
        AndroidWebConfigLocal config = new AndroidWebConfigLocal(env(Map.of(
                "APPIUM_URL", "not-a-url"
        )));

        IllegalStateException exception = assertThrows(IllegalStateException.class, config::createRequest);

        assertTrue(exception.getMessage().contains("APPIUM_URL must be a valid URL"));
    }

    private static Function<String, String> env(Map<String, String> values) {
        return values::get;
    }

    private static String lowerCaseCapability(DriverRequest request, String capabilityName) {
        Object value = request.getCapabilities().getCapability(capabilityName);
        return value == null ? null : String.valueOf(value).toLowerCase();
    }
}
