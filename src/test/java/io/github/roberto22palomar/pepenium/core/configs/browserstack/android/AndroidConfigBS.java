package io.github.roberto22palomar.pepenium.core.configs.browserstack.android;

import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.toolkit.utils.YamlLoader;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.openqa.selenium.MutableCapabilities;

import java.net.URL;
import java.time.Duration;

public class AndroidConfigBS implements DriverConfig {

    private final BrowserStackConfig config;

    // Leemos el browserstack.yml donde está la configuración
    public AndroidConfigBS() {
        config = YamlLoader.load("src/test/resources/browserstackAndroid.yml");
    }

    @Override
    public AppiumDriverLocalService startService() {
        // No arrancamos servicio local para BrowserStack: devolvemos null
        return null;
    }

    @Override
    public AppiumDriver createDriver(AppiumDriverLocalService service) throws Exception {
        BrowserStackConfig.Platform p = config.getPlatforms().get(0);

        // Capabilities para UiAutomator
        UiAutomator2Options opts = new UiAutomator2Options()
                .setAutomationName("UiAutomator2")
                .setPlatformName(p.getPlatformName())
                .setDeviceName(p.getDeviceName())
                .setPlatformVersion(p.getPlatformVersion())
                .setApp(config.getApp())
                .setNewCommandTimeout(Duration.ofSeconds(300));

        // Capabilities concretas de BrowserStack
        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("appProfiling", true);
        bstackOptions.setCapability("networkLogs", true);
        bstackOptions.setCapability("projectName", config.getProjectName());
        bstackOptions.setCapability("buildName", config.getBuildName());

        opts.setCapability("bstack:options", bstackOptions);

        // Capability en caso de que queramos lanzar en local, se configura en el browserstack.yaml
        opts.setCapability("browserstack.local", config.isBrowserstackLocal());


        URL remoteUrl = new URL(
                String.format(
                        "https://%s:%s@hub-cloud.browserstack.com/wd/hub",
                        config.getUserName(),
                        config.getAccessKey()
                )
        );

        return new AndroidDriver(remoteUrl, opts);
    }
}
