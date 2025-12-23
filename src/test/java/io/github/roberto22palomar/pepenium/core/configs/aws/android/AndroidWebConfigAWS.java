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

    private static final String DEFAULT_WEB_URL = "https://www.google.com/"; // Pon aquí tu URL

    @Override
    public AppiumDriverLocalService startService() {
        if (isRunningOnDeviceFarm()) {
            return null;
        } else {
            AppiumDriverLocalService service = new AppiumServiceBuilder()
                    .usingAnyFreePort()
                    .withArgument(() -> "--allow-insecure", "chromedriver_autodownload")
                    .build();
            service.start();
            return service;
        }
    }

    @Override
    public AppiumDriver createDriver(AppiumDriverLocalService service) {
        // Puedes usar UiAutomator2Options, pero para web normalmente se configuran las capabilities básicas
        UiAutomator2Options opts = new UiAutomator2Options()
                .setPlatformName("Android")
                .setDeviceName(System.getenv("DEVICEFARM_DEVICE_NAME"))
                .setAutomationName("UiAutomator2")
                .setNewCommandTimeout(Duration.ofSeconds(300));

        opts.setCapability("browserName", "Chrome");

        try {
            if (isRunningOnDeviceFarm()) {
                AppiumDriver driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), opts);
                // Navega a la URL deseada tras crear el driver
                driver.get(System.getenv().getOrDefault("DEVICEFARM_WEB_URL", DEFAULT_WEB_URL));
                return driver;
            } else {
                AppiumDriver driver = new AndroidDriver(service.getUrl(), opts);
                driver.get(DEFAULT_WEB_URL); // Local, abre la URL
                return driver;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isRunningOnDeviceFarm() {
        return System.getenv("DEVICEFARM_DEVICE_NAME") != null
                || System.getenv("AWS_DEVICE_FARM") != null;
    }
}
