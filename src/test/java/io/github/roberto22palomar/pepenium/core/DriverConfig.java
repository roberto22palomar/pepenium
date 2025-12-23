package io.github.roberto22palomar.pepenium.core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;

public interface DriverConfig {
    /** Arranca Appium y devuelve el servicio */
    AppiumDriverLocalService startService();

    /** Crea el driver apuntando al servicio recibido */
    AppiumDriver createDriver(AppiumDriverLocalService service) throws Exception;
}
