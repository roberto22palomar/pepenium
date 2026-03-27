package io.github.roberto22palomar.pepenium.core.configs.local.android;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.github.roberto22palomar.pepenium.core.config.validation.ConfigValidationSupport;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;

import java.time.Duration;

public class AndroidWebConfigLocal implements DriverConfig {

    @Override
    public DriverRequest createRequest() throws Exception {
        String appiumUrl = envOrDefault("APPIUM_URL", "http://localhost:4723");
        String udid = envOrDefault("ANDROID_UDID", "emulator-5554");
        String deviceName = envOrDefault("ANDROID_DEVICE_NAME", "Android Device");
        var serverUrl = ConfigValidationSupport.requireUrl(
                appiumUrl,
                "APPIUM_URL",
                "Use a valid Appium server URL such as http://localhost:4723."
        );

        UiAutomator2Options opts = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setDeviceName(deviceName)
                .setUdid(udid)
                .setNewCommandTimeout(Duration.ofSeconds(300));
        opts.setCapability("browserName", "Chrome");

        return DriverRequest.builder()
                .driverType(DriverType.ANDROID_APPIUM)
                .serverUrl(serverUrl)
                .capabilities(opts)
                .description("Local Android mobile web")
                .build();
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }
}
