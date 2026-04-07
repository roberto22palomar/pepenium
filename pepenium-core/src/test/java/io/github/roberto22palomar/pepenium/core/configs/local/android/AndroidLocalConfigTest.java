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
    void createAndroidNativeRequestAppliesOptionalAppiumCapabilityOverrides() throws Exception {
        AndroidConfigLocal config = new AndroidConfigLocal(env(Map.ofEntries(
                Map.entry("APP_PACKAGE", "io.github.example"),
                Map.entry("APP_ACTIVITY", ".MainActivity"),
                Map.entry("APPIUM_PLATFORM_NAME", "Android"),
                Map.entry("APPIUM_AUTOMATION_NAME", "UiAutomator2"),
                Map.entry("APPIUM_PLATFORM_VERSION", "15"),
                Map.entry("APPIUM_NEW_COMMAND_TIMEOUT", "120"),
                Map.entry("APPIUM_AUTO_GRANT_PERMISSIONS", "false"),
                Map.entry("APPIUM_NO_RESET", "true"),
                Map.entry("APPIUM_FULL_RESET", "true"),
                Map.entry("APPIUM_DONT_STOP_APP_ON_RESET", "true"),
                Map.entry("APPIUM_SKIP_DEVICE_INITIALIZATION", "true"),
                Map.entry("APPIUM_SKIP_SERVER_INSTALLATION", "true"),
                Map.entry("APPIUM_IGNORE_HIDDEN_API_POLICY_ERROR", "true"),
                Map.entry("APPIUM_AUTO_LAUNCH", "false"),
                Map.entry("APPIUM_ADB_EXEC_TIMEOUT", "45000"),
                Map.entry("APPIUM_UIAUTOMATOR2_SERVER_LAUNCH_TIMEOUT", "90000"),
                Map.entry("APPIUM_UIAUTOMATOR2_SERVER_INSTALL_TIMEOUT", "91000"),
                Map.entry("APPIUM_ANDROID_INSTALL_TIMEOUT", "92000"),
                Map.entry("APP_WAIT_PACKAGE", "io.github.example"),
                Map.entry("APP_WAIT_ACTIVITY", ".SplashActivity")
        )));

        DriverRequest request = config.createRequest();

        assertEquals("15", request.getCapabilities().getCapability("appium:platformVersion"));
        assertEquals("UiAutomator2", request.getCapabilities().getCapability("appium:automationName"));
        assertEquals(120L, request.getCapabilities().getCapability("appium:newCommandTimeout"));
        assertEquals(Boolean.FALSE, request.getCapabilities().getCapability("appium:autoGrantPermissions"));
        assertEquals(Boolean.TRUE, request.getCapabilities().getCapability("appium:noReset"));
        assertEquals(Boolean.TRUE, request.getCapabilities().getCapability("appium:fullReset"));
        assertEquals(Boolean.TRUE, request.getCapabilities().getCapability("appium:dontStopAppOnReset"));
        assertEquals(Boolean.TRUE, request.getCapabilities().getCapability("appium:skipDeviceInitialization"));
        assertEquals(Boolean.TRUE, request.getCapabilities().getCapability("appium:skipServerInstallation"));
        assertEquals(Boolean.TRUE, request.getCapabilities().getCapability("appium:ignoreHiddenApiPolicyError"));
        assertEquals(Boolean.FALSE, request.getCapabilities().getCapability("appium:autoLaunch"));
        assertEquals(45000L, request.getCapabilities().getCapability("appium:adbExecTimeout"));
        assertEquals(90000L, request.getCapabilities().getCapability("appium:uiautomator2ServerLaunchTimeout"));
        assertEquals(91000L, request.getCapabilities().getCapability("appium:uiautomator2ServerInstallTimeout"));
        assertEquals(92000L, request.getCapabilities().getCapability("appium:androidInstallTimeout"));
        assertEquals("io.github.example", request.getCapabilities().getCapability("appium:appWaitPackage"));
        assertEquals(".SplashActivity", request.getCapabilities().getCapability("appium:appWaitActivity"));
    }

    @Test
    void createAndroidNativeRequestRejectsInvalidBooleanOverride() {
        AndroidConfigLocal config = new AndroidConfigLocal(env(Map.of(
                "APP_PACKAGE", "io.github.example",
                "APP_ACTIVITY", ".MainActivity",
                "APPIUM_NO_RESET", "maybe"
        )));

        IllegalStateException exception = assertThrows(IllegalStateException.class, config::createRequest);

        assertTrue(exception.getMessage().contains("APPIUM_NO_RESET must be either 'true' or 'false'."));
    }

    @Test
    void createAndroidNativeRequestRejectsInvalidNumericOverride() {
        AndroidConfigLocal config = new AndroidConfigLocal(env(Map.of(
                "APP_PACKAGE", "io.github.example",
                "APP_ACTIVITY", ".MainActivity",
                "APPIUM_NEW_COMMAND_TIMEOUT", "abc"
        )));

        IllegalStateException exception = assertThrows(IllegalStateException.class, config::createRequest);

        assertTrue(exception.getMessage().contains("APPIUM_NEW_COMMAND_TIMEOUT must be a valid integer value."));
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
    void createAndroidWebRequestAppliesAppiumOverrides() throws Exception {
        AndroidWebConfigLocal config = new AndroidWebConfigLocal(env(Map.ofEntries(
                Map.entry("APPIUM_NEW_COMMAND_TIMEOUT", "90"),
                Map.entry("APPIUM_SKIP_DEVICE_INITIALIZATION", "true")
        )));

        DriverRequest request = config.createRequest();

        assertEquals(90L, request.getCapabilities().getCapability("appium:newCommandTimeout"));
        assertEquals(Boolean.TRUE, request.getCapabilities().getCapability("appium:skipDeviceInitialization"));
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
