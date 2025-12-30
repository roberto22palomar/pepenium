package io.github.roberto22palomar.pepenium.core.configs.browserstack.ios;

import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.toolkit.utils.YamlLoader;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.openqa.selenium.MutableCapabilities;

import java.net.URL;
import java.time.Duration;

public class IOSConfigBS implements DriverConfig {

    private final BrowserStackConfig config;

    // Loads BrowserStack iOS settings from a YAML file (device/app/build metadata and credentials).
    public IOSConfigBS() {
        config = YamlLoader.load("src/test/resources/browserstackIOS.yml");
    }

    @Override
    public AppiumDriverLocalService startService() {
        // BrowserStack provides a remote Appium server, so we do not start a local service.
        return null;
    }

    @Override
    public AppiumDriver createDriver(AppiumDriverLocalService service) throws Exception {
        BrowserStackConfig.Platform platform = config.getPlatforms().get(0);

        // Base Appium capabilities for iOS (XCUITest).
        XCUITestOptions opts = new XCUITestOptions()
                .setPlatformName(platform.getPlatformName())
                .setDeviceName(platform.getDeviceName())
                .setPlatformVersion(platform.getPlatformVersion())
                .setApp(config.getApp())
                .setNewCommandTimeout(Duration.ofSeconds(300));

        // BrowserStack-specific capabilities (reporting, metadata, logs).
        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("appProfiling", true);
        bstackOptions.setCapability("networkLogs", true);
        bstackOptions.setCapability("projectName", config.getProjectName());
        bstackOptions.setCapability("buildName", config.getBuildName());

        opts.setCapability("bstack:options", bstackOptions);

        // Enables BrowserStack Local if configured (useful for testing internal/staging environments).
        opts.setCapability("browserstack.local", config.isBrowserstackLocal());

        // Remote hub URL with BrowserStack credentials.
        URL remoteUrl = new URL(
                String.format(
                        "https://%s:%s@hub-cloud.browserstack.com/wd/hub",
                        config.getUserName(),
                        config.getAccessKey()
                )
        );

        return new IOSDriver(remoteUrl, opts);
    }
}
