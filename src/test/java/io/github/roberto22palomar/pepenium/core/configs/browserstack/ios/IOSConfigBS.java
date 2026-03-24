package io.github.roberto22palomar.pepenium.core.configs.browserstack.ios;

import io.appium.java_client.ios.options.XCUITestOptions;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.DriverRequest;
import io.github.roberto22palomar.pepenium.core.DriverType;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.toolkit.utils.YamlLoader;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import org.openqa.selenium.MutableCapabilities;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

public class IOSConfigBS implements DriverConfig {

    private static final String YAML_PATH = "src/test/resources/browserstackIOS.yml";

    private final BrowserStackConfig config;
    private final BrowserStackConfig.Platform platform;

    public IOSConfigBS() {
        this(loadConfig(), null);
    }

    public IOSConfigBS(BrowserStackConfig.Platform platform) {
        this(loadConfig(), platform);
    }

    private IOSConfigBS(BrowserStackConfig config, BrowserStackConfig.Platform platform) {
        this.config = config;
        this.platform = platform != null ? platform : getDefaultPlatform(config);
    }

    public static Stream<Arguments> platforms() {
        return getPlatforms(loadConfig()).stream()
                .map(platform -> Arguments.of(Named.of(platformLabel(platform), platform)));
    }

    @Override
    public DriverRequest createRequest() throws Exception {
        XCUITestOptions opts = new XCUITestOptions()
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

        return DriverRequest.builder()
                .driverType(DriverType.IOS_APPIUM)
                .serverUrl(remoteUrl)
                .capabilities(opts)
                .description("BrowserStack iOS native app - " + platformLabel(platform))
                .build();
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
        return String.format(
                "%s / %s / %s",
                platform.getPlatformName(),
                platform.getDeviceName(),
                platform.getPlatformVersion()
        );
    }
}
