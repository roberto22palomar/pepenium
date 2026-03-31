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
import java.util.function.Function;
import java.util.function.Supplier;

public class AndroidConfigAWS implements DriverConfig {

    private final Function<String, String> env;
    private final Supplier<AppiumServiceBuilder> serviceBuilderFactory;

    public AndroidConfigAWS() {
        this(System::getenv, AppiumServiceBuilder::new);
    }

    AndroidConfigAWS(Function<String, String> env, Supplier<AppiumServiceBuilder> serviceBuilderFactory) {
        this.env = env;
        this.serviceBuilderFactory = serviceBuilderFactory;
    }

    @Override
    public DriverRequest createRequest() throws Exception {
        AppiumDriverLocalService service = null;
        URL serverUrl;
        String deviceName = env.apply("DEVICEFARM_DEVICE_NAME");
        String appPath = env.apply("DEVICEFARM_APP_PATH");

        if (isRunningOnDeviceFarm()) {
            deviceName = ConfigValidationSupport.requireNonBlank(
                    deviceName,
                    "DEVICEFARM_DEVICE_NAME",
                    "AWS Android native runs need the device name provided by Device Farm."
            );
            appPath = ConfigValidationSupport.requireNonBlank(
                    appPath,
                    "DEVICEFARM_APP_PATH",
                    "AWS Android native runs need the app artifact path provided by Device Farm."
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

        UiAutomator2Options opts = new UiAutomator2Options()
                .setPlatformName("Android")
                .setDeviceName(deviceName)
                .setApp(appPath)
                .setAutomationName("UiAutomator2")
                .setNewCommandTimeout(Duration.ofSeconds(300))
                .setAutoGrantPermissions(true)
                .setNoReset(false);

        return DriverRequest.builder()
                .driverType(DriverType.ANDROID_APPIUM)
                .serverUrl(serverUrl)
                .capabilities(opts)
                .ownedService(service)
                .description("AWS Android native app")
                .build();
    }

    private boolean isRunningOnDeviceFarm() {
        return env.apply("DEVICEFARM_DEVICE_NAME") != null
                || env.apply("AWS_DEVICE_FARM") != null;
    }
}
