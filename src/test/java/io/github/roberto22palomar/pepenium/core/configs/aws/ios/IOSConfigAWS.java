package io.github.roberto22palomar.pepenium.core.configs.aws.ios;

import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;

import java.net.URL;
import java.time.Duration;

public class IOSConfigAWS implements DriverConfig {

    @Override
    public AppiumDriverLocalService startService() {
        // On AWS Device Farm, Appium is already running, so we must NOT start a local service.
        if (isRunningOnDeviceFarm()) {
            return null;
        }

        // Local execution: start an Appium service on any free port.
        AppiumDriverLocalService service = new AppiumServiceBuilder()
                .usingAnyFreePort()
                .withArgument(() -> "--allow-insecure", "chromedriver_autodownload")
                .build();

        service.start();
        return service;
    }

    @Override
    public AppiumDriver createDriver(AppiumDriverLocalService service) {
        XCUITestOptions opts = new XCUITestOptions()
                .setPlatformName("iOS")
                .setAutomationName("XCUITest")
                .setDeviceName(getEnvOrDefault("DEVICEFARM_DEVICE_NAME", "iPhone Simulator"))
                .setApp(getEnvOrDefault("DEVICEFARM_APP_PATH", System.getenv("IOS_APP_PATH")))
                .setNewCommandTimeout(Duration.ofSeconds(300))
                // Recommended timeouts for remote environments (e.g., WDA startup/connection).
                .setWdaLaunchTimeout(Duration.ofSeconds(120))
                .setWdaConnectionTimeout(Duration.ofSeconds(120))
                // Automatically handle common iOS system alerts.
                .setAutoAcceptAlerts(true)
                // Set to false to start with a clean state (optional).
                .setNoReset(false);

        try {
            if (isRunningOnDeviceFarm()) {
                // AWS: connect to the Appium server provided by Device Farm.
                return new IOSDriver(new URL("http://127.0.0.1:4723/wd/hub"), opts);
            }

            // Local: connect to the locally started Appium service.
            return new IOSDriver(service.getUrl(), opts);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create iOS driver", e);
        }
    }

    // ==========================================================
    // Helpers
    // ==========================================================

    private boolean isRunningOnDeviceFarm() {
        // Device Farm exposes environment variables that can be used to detect the runtime.
        return System.getenv("DEVICEFARM_DEVICE_NAME") != null
                || System.getenv("AWS_DEVICE_FARM") != null;
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String val = System.getenv(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }
}
