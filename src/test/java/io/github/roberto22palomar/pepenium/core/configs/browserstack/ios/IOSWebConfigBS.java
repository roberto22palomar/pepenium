package io.github.roberto22palomar.pepenium.core.configs.browserstack.ios;

import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfigMobile;
import io.github.roberto22palomar.pepenium.toolkit.utils.YamlLoaderMobile;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.openqa.selenium.MutableCapabilities;

import java.net.URL;

public class IOSWebConfigBS implements DriverConfig {

    private final BrowserStackConfigMobile config;

    // Loads BrowserStack iOS mobile-web settings from a YAML file (device/build metadata and credentials).
    public IOSWebConfigBS() {
        config = YamlLoaderMobile.load("src/test/resources/browserstackIOSWEB.yml");
    }

    @Override
    public AppiumDriver createDriver(AppiumDriverLocalService service) throws Exception {
        BrowserStackConfigMobile.Platform platform = config.getPlatforms().get(0);

        // Base capabilities for iOS mobile web testing (Safari).
        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("platformName", "iOS");
        caps.setCapability("browserName", "Safari");

        // BrowserStack-specific options (device selection, logs, build metadata).
        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("deviceName", platform.getDeviceName());
        // platformVersion can be set if you want to pin an iOS version:
        // bstackOptions.setCapability("platformVersion", platform.getPlatformVersion());
        bstackOptions.setCapability("projectName", config.getProjectName());
        bstackOptions.setCapability("buildName", config.getBuildName());
        bstackOptions.setCapability("appProfiling", true);
        bstackOptions.setCapability("networkLogs", true);
        bstackOptions.setCapability("local", config.isBrowserstackLocal());

        caps.setCapability("bstack:options", bstackOptions);

        // Remote hub URL with BrowserStack credentials.
        URL remoteUrl = new URL(
                String.format(
                        "https://%s:%s@hub-cloud.browserstack.com/wd/hub",
                        config.getUserName(),
                        config.getAccessKey()
                )
        );

        return new IOSDriver(remoteUrl, caps);
    }

    @Override
    public AppiumDriverLocalService startService() {
        // BrowserStack provides a remote Appium server, so we do not start a local service.
        return null;
    }
}
