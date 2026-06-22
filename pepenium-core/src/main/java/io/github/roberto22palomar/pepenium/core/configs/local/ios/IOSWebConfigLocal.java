package io.github.roberto22palomar.pepenium.core.configs.local.ios;

import io.appium.java_client.ios.options.XCUITestOptions;
import io.github.roberto22palomar.pepenium.core.config.PepeniumConfig;
import io.github.roberto22palomar.pepenium.core.config.validation.AppiumCapabilityOverrides;
import io.github.roberto22palomar.pepenium.core.config.validation.ConfigValidationSupport;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;

import java.time.Duration;
import java.util.function.Function;

public class IOSWebConfigLocal implements DriverConfig {

    private final Function<String, String> env;

    public IOSWebConfigLocal() {
        this(PepeniumConfig::get);
    }

    IOSWebConfigLocal(Function<String, String> env) {
        this.env = env;
    }

    @Override
    public DriverRequest createRequest() throws Exception {
        String appiumUrl = envOrDefault("APPIUM_URL", "http://localhost:4723");
        String deviceName = envOrDefault("IOS_DEVICE_NAME", "iPhone Simulator");
        String udid = envValue("IOS_UDID");
        String platformVersion = envValue("IOS_PLATFORM_VERSION");

        XCUITestOptions options = new XCUITestOptions()
                .setPlatformName("iOS")
                .setAutomationName("XCUITest")
                .setDeviceName(deviceName)
                .setNewCommandTimeout(Duration.ofSeconds(300));
        options.setCapability("browserName", "Safari");
        if (udid != null) {
            options.setUdid(udid);
        }
        if (platformVersion != null) {
            options.setPlatformVersion(platformVersion);
        }
        AppiumCapabilityOverrides.applyIos(env, options);

        return DriverRequest.builder()
                .driverType(DriverType.IOS_APPIUM)
                .serverUrl(ConfigValidationSupport.requireUrl(
                        appiumUrl,
                        "APPIUM_URL",
                        "Use a valid Appium server URL such as http://localhost:4723."
                ))
                .capabilities(options)
                .description("Local iOS mobile web")
                .build();
    }

    private String envOrDefault(String key, String defaultValue) {
        String value = envValue(key);
        return value == null ? defaultValue : value;
    }

    private String envValue(String key) {
        String value = env.apply(key);
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
