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
        // En AWS Device Farm ya hay Appium levantado, no lo iniciamos localmente
        if (isRunningOnDeviceFarm()) {
            return null;
        }

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
                // Opcionales pero recomendables en AWS
                .setWdaLaunchTimeout(Duration.ofSeconds(120))
                .setWdaConnectionTimeout(Duration.ofSeconds(120))
                .setAutoAcceptAlerts(true)
                .setNoReset(false);

        try {
            if (isRunningOnDeviceFarm()) {
                // En AWS Device Farm, Appium ya escucha en localhost
                return new IOSDriver(new URL("http://127.0.0.1:4723/wd/hub"), opts);
            } else {
                // En local, usamos el servicio recién levantado
                return new IOSDriver(service.getUrl(), opts);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el driver iOS", e);
        }
    }

    // ==========================================================
    // Helpers
    // ==========================================================

    private boolean isRunningOnDeviceFarm() {
        return System.getenv("DEVICEFARM_DEVICE_NAME") != null
                || System.getenv("AWS_DEVICE_FARM") != null;
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String val = System.getenv(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }
}
