package io.github.roberto22palomar.pepenium.core.configs.aws.ios;

import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import io.github.roberto22palomar.pepenium.core.config.validation.ConfigValidationSupport;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;

import java.net.URL;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

public class IOSConfigAWS implements DriverConfig {

    private final Function<String, String> env;
    private final Supplier<AppiumServiceBuilder> serviceBuilderFactory;

    public IOSConfigAWS() {
        this(System::getenv, AppiumServiceBuilder::new);
    }

    IOSConfigAWS(Function<String, String> env, Supplier<AppiumServiceBuilder> serviceBuilderFactory) {
        this.env = env;
        this.serviceBuilderFactory = serviceBuilderFactory;
    }

    @Override
    public DriverRequest createRequest() throws Exception {
        AppiumDriverLocalService service = null;
        URL serverUrl;
        String deviceName = getEnvOrDefault("DEVICEFARM_DEVICE_NAME", "iPhone Simulator");
        String appPath = getEnvOrDefault("DEVICEFARM_APP_PATH", env.apply("IOS_APP_PATH"));

        if (isRunningOnDeviceFarm()) {
            deviceName = ConfigValidationSupport.requireNonBlank(
                    env.apply("DEVICEFARM_DEVICE_NAME"),
                    "DEVICEFARM_DEVICE_NAME",
                    "AWS iOS native runs need the device name provided by Device Farm."
            );
            appPath = ConfigValidationSupport.requireNonBlank(
                    appPath,
                    "DEVICEFARM_APP_PATH / IOS_APP_PATH",
                    "AWS iOS native runs need DEVICEFARM_APP_PATH or IOS_APP_PATH."
            );
            serverUrl = new URL("http://127.0.0.1:4723/wd/hub");
        } else {
            service = serviceBuilderFactory.get()
                    .usingAnyFreePort()
                    .withArgument(() -> "--allow-insecure", "chromedriver_autodownload")
                    .build();
            service.start();
            serverUrl = service.getUrl();
        }

        XCUITestOptions opts = new XCUITestOptions()
                .setPlatformName("iOS")
                .setAutomationName("XCUITest")
                .setDeviceName(deviceName)
                .setApp(appPath)
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
        return env.apply("DEVICEFARM_DEVICE_NAME") != null
                || env.apply("AWS_DEVICE_FARM") != null;
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = env.apply(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
