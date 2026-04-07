package io.github.roberto22palomar.pepenium.core.configs.browserstack.ios;

import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfigMobile;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IOSBrowserStackConfigTest {

    @Test
    void createRequestAppliesIosAppiumOverrides() throws Exception {
        BrowserStackConfig config = BrowserStackConfig.builder()
                .userName("user")
                .accessKey("key")
                .app("bs://app")
                .projectName("project")
                .buildName("build")
                .platforms(List.of())
                .build();
        BrowserStackConfig.Platform platform = BrowserStackConfig.Platform.builder()
                .platformName("ios")
                .deviceName("iPhone 16")
                .platformVersion("18")
                .build();

        IOSConfigBS driverConfig = new IOSConfigBS(config, platform, Map.of(
                "APPIUM_NEW_COMMAND_TIMEOUT", "180",
                "APPIUM_AUTO_ACCEPT_ALERTS", "false",
                "APPIUM_WDA_LAUNCH_TIMEOUT", "60"
        )::get);

        DriverRequest request = driverConfig.createRequest();

        assertEquals(180L, request.getCapabilities().getCapability("appium:newCommandTimeout"));
        assertEquals(Boolean.FALSE, request.getCapabilities().getCapability("appium:autoAcceptAlerts"));
        assertEquals(60000L, request.getCapabilities().getCapability("appium:wdaLaunchTimeout"));
    }

    @Test
    void createWebRequestAppliesIosAppiumOverrides() throws Exception {
        BrowserStackConfigMobile config = BrowserStackConfigMobile.builder()
                .userName("user")
                .accessKey("key")
                .projectName("project")
                .buildName("build")
                .platforms(List.of())
                .build();
        BrowserStackConfigMobile.Platform platform = BrowserStackConfigMobile.Platform.builder()
                .deviceName("iPhone 16")
                .osVersion("18")
                .browserName("Safari")
                .build();

        IOSWebConfigBS driverConfig = new IOSWebConfigBS(config, platform, Map.of(
                "APPIUM_NEW_COMMAND_TIMEOUT", "150",
                "APPIUM_AUTO_ACCEPT_ALERTS", "false"
        )::get);

        DriverRequest request = driverConfig.createRequest();

        assertEquals(150L, request.getCapabilities().getCapability("appium:newCommandTimeout"));
        assertEquals(Boolean.FALSE, request.getCapabilities().getCapability("appium:autoAcceptAlerts"));
    }
}
