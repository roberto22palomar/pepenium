package io.github.roberto22palomar.pepenium.core.configs.local.android;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.DriverRequest;
import io.github.roberto22palomar.pepenium.core.DriverType;

import java.net.URL;
import java.time.Duration;

public class AndroidConfigLocal implements DriverConfig {

    @Override
    public DriverRequest createRequest() throws Exception {
        String appiumUrl = envOrDefault("APPIUM_URL", "http://localhost:4723");
        String udid = envOrDefault("ANDROID_UDID", "emulator-5554");
        String deviceName = envOrDefault("ANDROID_DEVICE_NAME", "Android Device");

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
            opts.setApp(stripQuotes(appPath));
        }

        String appPackage = System.getenv("APP_PACKAGE");
        String appActivity = System.getenv("APP_ACTIVITY");
        if (notBlank(appPackage) && notBlank(appActivity)) {
            opts.setAppPackage(appPackage);
            opts.setAppActivity(appActivity);
        }

        return DriverRequest.builder()
                .driverType(DriverType.ANDROID_APPIUM)
                .serverUrl(new URL(appiumUrl))
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
