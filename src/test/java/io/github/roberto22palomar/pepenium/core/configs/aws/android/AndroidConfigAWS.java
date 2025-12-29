package io.github.roberto22palomar.pepenium.core.configs.aws.android;

import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;

import java.net.URL;
import java.time.Duration;

public class AndroidConfigAWS implements DriverConfig {

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
        UiAutomator2Options opts = new UiAutomator2Options()
                .setPlatformName("Android")
                .setDeviceName(System.getenv("DEVICEFARM_DEVICE_NAME"))
                .setApp(System.getenv("DEVICEFARM_APP_PATH"))
                .setAutomationName("UiAutomator2")
                .setNewCommandTimeout(Duration.ofSeconds(300))
                // Helpful for handling runtime permission dialogs automatically.
                .setAutoGrantPermissions(true)
                // Set to false to start with a clean state (optional).
                .setNoReset(false);

        // If your Appium Java Client version does not support setAutoGrantPermissions(),
        // you can use: opts.amend("autoGrantPermissions", true);

        try {
            if (isRunningOnDeviceFarm()) {
                // AWS: connect to the Appium server provided by Device Farm.
                return new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), opts);
            }

            // Local: connect to the locally started Appium service.
            return new AndroidDriver(service.getUrl(), opts);

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
