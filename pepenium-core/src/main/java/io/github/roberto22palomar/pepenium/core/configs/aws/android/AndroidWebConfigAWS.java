package io.github.roberto22palomar.pepenium.core.configs.aws.android;

import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import io.github.roberto22palomar.pepenium.core.config.validation.ConfigValidationSupport;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;

import java.net.URL;
import java.time.Duration;

public class AndroidWebConfigAWS implements DriverConfig {

    @Override
    public DriverRequest createRequest() throws Exception {
        AppiumDriverLocalService service = null;
        URL serverUrl;
        String deviceName = System.getenv("DEVICEFARM_DEVICE_NAME");

        if (isRunningOnDeviceFarm()) {
            deviceName = ConfigValidationSupport.requireNonBlank(
                    deviceName,
                    "DEVICEFARM_DEVICE_NAME",
                    "AWS Android web runs need the device name provided by Device Farm."
            );
            serverUrl = new URL("http://127.0.0.1:4723");
        } else {
            service = new AppiumServiceBuilder()
                    .usingAnyFreePort()
                    .withArgument(() -> "--allow-insecure", "chromedriver_autodownload")
                    .build();
            service.start();
            serverUrl = service.getUrl();
        }

        UiAutomator2Options opts = new UiAutomator2Options()
                .setPlatformName("Android")
                .setDeviceName(deviceName)
                .setAutomationName("UiAutomator2")
                .setNewCommandTimeout(Duration.ofSeconds(300));
        opts.setCapability("browserName", "Chrome");

        return DriverRequest.builder()
                .driverType(DriverType.ANDROID_APPIUM)
                .serverUrl(serverUrl)
                .capabilities(opts)
                .ownedService(service)
                .description("AWS Android mobile web")
                .build();
    }

    private boolean isRunningOnDeviceFarm() {
        return System.getenv("DEVICEFARM_DEVICE_NAME") != null
                || System.getenv("AWS_DEVICE_FARM") != null;
    }
}
