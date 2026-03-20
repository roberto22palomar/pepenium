package io.github.roberto22palomar.pepenium.core.configs.browserstack.android;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.toolkit.utils.YamlLoader;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import org.openqa.selenium.MutableCapabilities;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

public class AndroidConfigBS implements DriverConfig {

    private static final String YAML_PATH = "src/test/resources/browserstackAndroid.yml";

    private final BrowserStackConfig config;
    private final BrowserStackConfig.Platform platform;

    public AndroidConfigBS() {
        this(loadConfig(), null);
    }

    public AndroidConfigBS(BrowserStackConfig.Platform platform) {
        this(loadConfig(), platform);
    }

    private AndroidConfigBS(BrowserStackConfig config, BrowserStackConfig.Platform platform) {
        this.config = config;
        this.platform = platform != null ? platform : getDefaultPlatform(config);
    }

    public static Stream<Arguments> platforms() {
        return getPlatforms(loadConfig()).stream()
                .map(platform -> Arguments.of(Named.of(platformLabel(platform), platform)));
    }

    @Override
    public AppiumDriverLocalService startService() {
        return null;
    }

    @Override
    public AppiumDriver createDriver(AppiumDriverLocalService service) throws Exception {
        UiAutomator2Options opts = new UiAutomator2Options()
                .setAutomationName("UiAutomator2")
                .setPlatformName(platform.getPlatformName())
                .setDeviceName(platform.getDeviceName())
                .setPlatformVersion(platform.getPlatformVersion())
                .setApp(config.getApp())
                .setNewCommandTimeout(Duration.ofSeconds(300));

        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("appProfiling", true);
        bstackOptions.setCapability("networkLogs", true);
        bstackOptions.setCapability("projectName", config.getProjectName());
        bstackOptions.setCapability("buildName", config.getBuildName());

        opts.setCapability("bstack:options", bstackOptions);
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

    private static BrowserStackConfig loadConfig() {
        return YamlLoader.load(YAML_PATH);
    }

    private static List<BrowserStackConfig.Platform> getPlatforms(BrowserStackConfig config) {
        if (config == null || config.getPlatforms() == null || config.getPlatforms().isEmpty()) {
            throw new IllegalStateException("No BrowserStack platforms were found in " + YAML_PATH);
        }
        return config.getPlatforms();
    }

    private static BrowserStackConfig.Platform getDefaultPlatform(BrowserStackConfig config) {
        return getPlatforms(config).get(0);
    }

    private static String platformLabel(BrowserStackConfig.Platform platform) {
        return platform.getDeviceName() + " / Android " + platform.getPlatformVersion();
    }
}
