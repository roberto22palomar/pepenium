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
        // Si estamos en AWS Device Farm, NO arrancamos servicio local
        if (isRunningOnDeviceFarm()) {
            return null; // Indicamos que NO hay Appium local arrancado
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
        UiAutomator2Options opts = new UiAutomator2Options()
                .setPlatformName("Android")
                .setDeviceName(System.getenv("DEVICEFARM_DEVICE_NAME"))
                .setApp(System.getenv("DEVICEFARM_APP_PATH"))
                .setAutomationName("UiAutomator2")
                .setNewCommandTimeout(Duration.ofSeconds(300))

                // 👇 Lo importante para el popup de permisos
                .setAutoGrantPermissions(true)
                // Opcional: si quieres asegurarte de que siempre empieza “limpia”
                .setNoReset(false);

        // Si tu versión de cliente no tuviera setAutoGrantPermissions,
        // equivalente sería:
        // opts.amend("autoGrantPermissions", true);

        try {
            if (isRunningOnDeviceFarm()) {
                // AWS: conéctate al Appium ya levantado
                return new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), opts);
            } else {
                // Local: conecta usando el servicio local (recién arrancado)
                return new AndroidDriver(service.getUrl(), opts);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isRunningOnDeviceFarm() {
        // Device Farm SIEMPRE expone alguna de estas variables
        return System.getenv("DEVICEFARM_DEVICE_NAME") != null
                || System.getenv("AWS_DEVICE_FARM") != null;
    }
}
