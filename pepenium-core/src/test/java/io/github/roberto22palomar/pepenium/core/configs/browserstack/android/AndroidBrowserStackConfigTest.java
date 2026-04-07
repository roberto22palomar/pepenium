package io.github.roberto22palomar.pepenium.core.configs.browserstack.android;

import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfigMobile;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AndroidBrowserStackConfigTest {

    @Test
    void createRequestAppliesAndroidAppiumOverrides() throws Exception {
        BrowserStackConfig config = BrowserStackConfig.builder()
                .userName("user")
                .accessKey("key")
                .app("bs://app")
                .projectName("project")
                .buildName("build")
                .platforms(List.of())
                .build();
        BrowserStackConfig.Platform platform = BrowserStackConfig.Platform.builder()
                .platformName("android")
                .deviceName("Pixel 9")
                .platformVersion("15")
                .build();

        AndroidConfigBS driverConfig = new AndroidConfigBS(config, platform, Map.of(
                "APPIUM_NEW_COMMAND_TIMEOUT", "120",
                "APPIUM_AUTO_GRANT_PERMISSIONS", "false",
                "APP_WAIT_ACTIVITY", ".SplashActivity"
        )::get);

        DriverRequest request = driverConfig.createRequest();

        assertEquals(120L, request.getCapabilities().getCapability("appium:newCommandTimeout"));
        assertEquals(Boolean.FALSE, request.getCapabilities().getCapability("appium:autoGrantPermissions"));
        assertEquals(".SplashActivity", request.getCapabilities().getCapability("appium:appWaitActivity"));
    }

    @Test
    void createWebRequestAppliesAndroidAppiumOverrides() throws Exception {
        BrowserStackConfigMobile config = BrowserStackConfigMobile.builder()
                .userName("user")
                .accessKey("key")
                .projectName("project")
                .buildName("build")
                .platforms(List.of())
                .build();
        BrowserStackConfigMobile.Platform platform = BrowserStackConfigMobile.Platform.builder()
                .deviceName("Pixel 9")
                .osVersion("15")
                .browserName("Chrome")
                .build();

        AndroidWebConfigBS driverConfig = new AndroidWebConfigBS(config, platform, Map.of(
                "APPIUM_NEW_COMMAND_TIMEOUT", "90",
                "APPIUM_SKIP_DEVICE_INITIALIZATION", "true"
        )::get);

        DriverRequest request = driverConfig.createRequest();

        assertEquals(90L, request.getCapabilities().getCapability("appium:newCommandTimeout"));
        assertEquals(Boolean.TRUE, request.getCapabilities().getCapability("appium:skipDeviceInitialization"));
    }
}
