package io.github.roberto22palomar.pepenium.core.configs.browserstack.android;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfigMobile;
import io.github.roberto22palomar.pepenium.toolkit.utils.YamlLoaderMobile;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import org.openqa.selenium.MutableCapabilities;

import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

public class AndroidWebConfigBS implements DriverConfig {

    private static final String YAML_PATH = "src/test/resources/browserstackAndroidWEB.yml";

    private final BrowserStackConfigMobile config;
    private final BrowserStackConfigMobile.Platform platform;

    public AndroidWebConfigBS() {
        this(loadConfig(), null);
    }

    public AndroidWebConfigBS(BrowserStackConfigMobile.Platform platform) {
        this(loadConfig(), platform);
    }

    private AndroidWebConfigBS(BrowserStackConfigMobile config, BrowserStackConfigMobile.Platform platform) {
        this.config = config;
        this.platform = platform != null ? platform : getDefaultPlatform(config);
    }

    public static Stream<Arguments> platforms() {
        return getPlatforms(loadConfig()).stream()
                .map(platform -> Arguments.of(Named.of(platformLabel(platform), platform)));
    }

    @Override
    public AppiumDriver createDriver(AppiumDriverLocalService service) throws Exception {
        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("browserName", "Chrome");

        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("deviceName", platform.getDeviceName());
        bstackOptions.setCapability("osVersion", platform.getOsVersion());
        bstackOptions.setCapability("projectName", config.getProjectName());
        bstackOptions.setCapability("buildName", config.getBuildName());
        bstackOptions.setCapability("appProfiling", true);
        bstackOptions.setCapability("networkLogs", true);
        bstackOptions.setCapability("local", config.isBrowserstackLocal());
        if (platform.getDeviceOrientation() != null) {
            bstackOptions.setCapability("deviceOrientation", platform.getDeviceOrientation());
        }
        if (platform.getBrowserName() != null) {
            caps.setCapability("browserName", platform.getBrowserName());
        }

        caps.setCapability("bstack:options", bstackOptions);

        URL remoteUrl = new URL(
                String.format(
                        "https://%s:%s@hub-cloud.browserstack.com/wd/hub",
                        config.getUserName(),
                        config.getAccessKey()
                )
        );

        return new AndroidDriver(remoteUrl, caps);
    }

    @Override
    public AppiumDriverLocalService startService() {
        return null;
    }

    private static BrowserStackConfigMobile loadConfig() {
        return YamlLoaderMobile.load(YAML_PATH);
    }

    private static List<BrowserStackConfigMobile.Platform> getPlatforms(BrowserStackConfigMobile config) {
        if (config == null || config.getPlatforms() == null || config.getPlatforms().isEmpty()) {
            throw new IllegalStateException("No BrowserStack platforms were found in " + YAML_PATH);
        }
        return config.getPlatforms();
    }

    private static BrowserStackConfigMobile.Platform getDefaultPlatform(BrowserStackConfigMobile config) {
        return getPlatforms(config).get(0);
    }

    private static String platformLabel(BrowserStackConfigMobile.Platform platform) {
        return platform.getDeviceName() + " / Android " + platform.getOsVersion();
    }
}
