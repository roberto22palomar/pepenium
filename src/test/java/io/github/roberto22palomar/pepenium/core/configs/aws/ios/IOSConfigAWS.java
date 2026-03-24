package io.github.roberto22palomar.pepenium.core.configs.aws.ios;

import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.DriverRequest;
import io.github.roberto22palomar.pepenium.core.DriverType;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;

import java.net.URL;
import java.time.Duration;

public class IOSConfigAWS implements DriverConfig {

    @Override
    public DriverRequest createRequest() throws Exception {
        AppiumDriverLocalService service = null;
        URL serverUrl;

        if (isRunningOnDeviceFarm()) {
            serverUrl = new URL("http://127.0.0.1:4723/wd/hub");
        } else {
            service = new AppiumServiceBuilder()
                    .usingAnyFreePort()
                    .withArgument(() -> "--allow-insecure", "chromedriver_autodownload")
                    .build();
            service.start();
            serverUrl = service.getUrl();
        }

        XCUITestOptions opts = new XCUITestOptions()
                .setPlatformName("iOS")
                .setAutomationName("XCUITest")
                .setDeviceName(getEnvOrDefault("DEVICEFARM_DEVICE_NAME", "iPhone Simulator"))
                .setApp(getEnvOrDefault("DEVICEFARM_APP_PATH", System.getenv("IOS_APP_PATH")))
                .setNewCommandTimeout(Duration.ofSeconds(300))
                .setWdaLaunchTimeout(Duration.ofSeconds(120))
                .setWdaConnectionTimeout(Duration.ofSeconds(120))
                .setAutoAcceptAlerts(true)
                .setNoReset(false);

        return DriverRequest.builder()
                .driverType(DriverType.IOS_APPIUM)
                .serverUrl(serverUrl)
                .capabilities(opts)
                .ownedService(service)
                .description("AWS iOS native app")
                .build();
    }

    private boolean isRunningOnDeviceFarm() {
        return System.getenv("DEVICEFARM_DEVICE_NAME") != null
                || System.getenv("AWS_DEVICE_FARM") != null;
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
