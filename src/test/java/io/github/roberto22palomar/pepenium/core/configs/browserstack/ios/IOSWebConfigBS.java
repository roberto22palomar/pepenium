package io.github.roberto22palomar.pepenium.core.configs.browserstack.ios;

import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import io.github.roberto22palomar.pepenium.toolkit.browserstack.BrowserStackConfigMobile;
import io.github.roberto22palomar.pepenium.toolkit.yaml.YamlLoaderMobile;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import org.openqa.selenium.MutableCapabilities;

import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

public class IOSWebConfigBS implements DriverConfig {

    private static final String YAML_PATH = "src/test/resources/browserstackIOSWEB.yml";

    private final BrowserStackConfigMobile config;
    private final BrowserStackConfigMobile.Platform platform;

    public IOSWebConfigBS() {
        this(loadConfig(), null);
    }

    public IOSWebConfigBS(BrowserStackConfigMobile.Platform platform) {
        this(loadConfig(), platform);
    }

    private IOSWebConfigBS(BrowserStackConfigMobile config, BrowserStackConfigMobile.Platform platform) {
        this.config = config;
        this.platform = platform != null ? platform : getDefaultPlatform(config);
    }

    public static Stream<Arguments> platforms() {
        return getPlatforms(loadConfig()).stream()
                .map(platform -> Arguments.of(Named.of(platformLabel(platform), platform)));
    }

    @Override
    public DriverRequest createRequest() throws Exception {
        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("platformName", "iOS");
        caps.setCapability("browserName", "Safari");

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

        return DriverRequest.builder()
                .driverType(DriverType.IOS_APPIUM)
                .serverUrl(remoteUrl)
                .capabilities(caps)
                .description("BrowserStack iOS web - " + platformLabel(platform))
                .build();
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
        return String.format(
                "%s / %s / %s",
                platform.getDeviceName(),
                platform.getOsVersion(),
                platform.getBrowserName() != null ? platform.getBrowserName() : "Safari"
        );
    }
}
