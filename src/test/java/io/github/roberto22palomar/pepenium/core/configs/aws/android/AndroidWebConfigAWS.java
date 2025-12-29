package io.github.roberto22palomar.pepenium.core.configs.aws.android;

import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;

import java.net.URL;
import java.time.Duration;

public class AndroidWebConfigAWS implements DriverConfig {

    // Default URL used for local runs (can be overridden in Device Farm via env vars).
    private static final String DEFAULT_WEB_URL = "https://www.google.com/";

    @Override
    public AppiumDriverLocalService startService() {
        // On AWS Device Farm, Appium is already running.
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
        // For mobile web automation we still use UiAutomator2, but set the browserName capability.
        UiAutomator2Options opts = new UiAutomator2Options()
                .setPlatformName("Android")
                .setDeviceName(System.getenv("DEVICEFARM_DEVICE_NAME"))
                .setAutomationName("UiAutomator2")
                .setNewCommandTimeout(Duration.ofSeconds(300));

        opts.setCapability("browserName", "Chrome");

        try {
            AppiumDriver driver;

            if (isRunningOnDeviceFarm()) {
                // AWS: connect to the Appium server provided by Device Farm.
                driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), opts);

                // Device Farm can provide a target URL via env var; fallback to default.
                String url = System.getenv().getOrDefault("DEVICEFARM_WEB_URL", DEFAULT_WEB_URL);
                driver.get(url);
                return driver;
            }

            // Local: connect to the locally started Appium service.
            driver = new AndroidDriver(service.getUrl(), opts);
            driver.get(DEFAULT_WEB_URL);
            return driver;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isRunningOnDeviceFarm() {
        // Device Farm exposes environment variables that can be used to detect the runtime.
        return System.getenv("DEVICEFARM_DEVICE_NAME") != null
                || System.getenv("AWS_DEVICE_FARM") != null;
    }
}
