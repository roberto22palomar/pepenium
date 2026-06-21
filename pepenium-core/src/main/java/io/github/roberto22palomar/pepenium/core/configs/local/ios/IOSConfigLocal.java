package io.github.roberto22palomar.pepenium.core.configs.local.ios;

import io.appium.java_client.ios.options.XCUITestOptions;
import io.github.roberto22palomar.pepenium.core.config.PepeniumConfig;
import io.github.roberto22palomar.pepenium.core.config.validation.AppiumCapabilityOverrides;
import io.github.roberto22palomar.pepenium.core.config.validation.ConfigValidationSupport;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Function;

public class IOSConfigLocal implements DriverConfig {

    private final Function<String, String> env;

    public IOSConfigLocal() {
        this(PepeniumConfig::get);
    }

    IOSConfigLocal(Function<String, String> env) {
        this.env = env;
    }

    @Override
    public DriverRequest createRequest() throws Exception {
        String appiumUrl = envOrDefault("APPIUM_URL", "http://localhost:4723");
        String deviceName = envOrDefault("IOS_DEVICE_NAME", "iPhone Simulator");
        String udid = envValue("IOS_UDID");
        String platformVersion = envValue("IOS_PLATFORM_VERSION");
        String appPath = firstFilled(envValue("IOS_APP_PATH"), envValue("APP_PATH"));
        String bundleId = envValue("IOS_BUNDLE_ID");

        XCUITestOptions options = baseOptions(deviceName, udid, platformVersion);
        if (notBlank(appPath)) {
            options.setApp(ConfigValidationSupport.requireExistingFile(
                    stripQuotes(appPath),
                    "IOS_APP_PATH / APP_PATH",
                    "Point it to an existing .app directory or .ipa file for local iOS native tests."
            ));
        }
        if (notBlank(bundleId)) {
            options.setBundleId(bundleId);
        }
        AppiumCapabilityOverrides.applyIos(env, options);
        ConfigValidationSupport.requireAtLeastOneFilled(
                "Local iOS native app configuration",
                Arrays.asList(appPath, bundleId),
                "Provide IOS_APP_PATH (or APP_PATH), or provide IOS_BUNDLE_ID for an installed app."
        );

        return DriverRequest.builder()
                .driverType(DriverType.IOS_APPIUM)
                .serverUrl(ConfigValidationSupport.requireUrl(
                        appiumUrl,
                        "APPIUM_URL",
                        "Use a valid Appium server URL such as http://localhost:4723."
                ))
                .capabilities(options)
                .description("Local iOS native app")
                .build();
    }

    private XCUITestOptions baseOptions(String deviceName, String udid, String platformVersion) {
        XCUITestOptions options = new XCUITestOptions()
                .setPlatformName("iOS")
                .setAutomationName("XCUITest")
                .setDeviceName(deviceName)
                .setNewCommandTimeout(Duration.ofSeconds(300))
                .setWdaLaunchTimeout(Duration.ofSeconds(120))
                .setWdaConnectionTimeout(Duration.ofSeconds(120))
                .setAutoAcceptAlerts(true)
                .setNoReset(false);
        if (notBlank(udid)) {
            options.setUdid(udid);
        }
        if (notBlank(platformVersion)) {
            options.setPlatformVersion(platformVersion);
        }
        return options;
    }

    private String envOrDefault(String key, String defaultValue) {
        String value = envValue(key);
        return value == null ? defaultValue : value;
    }

    private String envValue(String key) {
        String value = env.apply(key);
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static String firstFilled(String first, String second) {
        return notBlank(first) ? first : second;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String stripQuotes(String value) {
        return value == null ? null : value.replace("\"", "").trim();
    }
}
