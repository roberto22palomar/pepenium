package io.github.roberto22palomar.pepenium.core.configs.local.android;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.github.roberto22palomar.pepenium.core.config.validation.ConfigValidationSupport;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;

import java.time.Duration;
import java.util.List;

public class AndroidConfigLocal implements DriverConfig {

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
                .setNewCommandTimeout(Duration.ofSeconds(300))
                .setAutoGrantPermissions(true)
                .setNoReset(false)
                .setFullReset(false);

        String appPath = System.getenv("APP_PATH");
        if (notBlank(appPath)) {
            opts.setApp(ConfigValidationSupport.requireExistingFile(
                    stripQuotes(appPath),
                    "APP_PATH",
                    "Point it to a valid APK when running local Android native tests."
            ));
        }

        String appPackage = System.getenv("APP_PACKAGE");
        String appActivity = System.getenv("APP_ACTIVITY");
        if (notBlank(appPackage) && notBlank(appActivity)) {
            opts.setAppPackage(appPackage);
            opts.setAppActivity(appActivity);
        }

        ConfigValidationSupport.requireAtLeastOneFilled(
                "Local Android native app configuration",
                List.of(appPath, appPackage),
                "Provide APP_PATH, or provide both APP_PACKAGE and APP_ACTIVITY."
        );
        if (notBlank(appPackage) || notBlank(appActivity)) {
            ConfigValidationSupport.requireAllFilled(
                    "Local Android native package/activity configuration",
                    List.of(appPackage, appActivity),
                    "APP_PACKAGE and APP_ACTIVITY must be provided together."
            );
        }

        return DriverRequest.builder()
                .driverType(DriverType.ANDROID_APPIUM)
                .serverUrl(serverUrl)
                .capabilities(opts)
                .description("Local Android native app")
                .build();
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String stripQuotes(String value) {
        return value == null ? null : value.replace("\"", "").trim();
    }
}
