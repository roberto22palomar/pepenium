package io.github.roberto22palomar.pepenium.core.configs.browserstack.desktop;

import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import io.github.roberto22palomar.pepenium.toolkit.browserstack.BrowserStackConfigDesktop;
import io.github.roberto22palomar.pepenium.toolkit.yaml.YamlLoaderDesktop;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import org.openqa.selenium.MutableCapabilities;

import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

public class MacWebConfigBS implements DriverConfig {

    private static final String YAML_PATH = "src/test/resources/browserstackMac.yml";

    private final BrowserStackConfigDesktop config;
    private final BrowserStackConfigDesktop.Platform platform;

    public MacWebConfigBS() {
        this(loadConfig(), null);
    }

    public MacWebConfigBS(BrowserStackConfigDesktop.Platform platform) {
        this(loadConfig(), platform);
    }

    private MacWebConfigBS(BrowserStackConfigDesktop config, BrowserStackConfigDesktop.Platform platform) {
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
        caps.setCapability("browserName", platform.getBrowserName());
        caps.setCapability("browserVersion", platform.getBrowserVersion());

        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("os", platform.getOs());
        bstackOptions.setCapability("osVersion", platform.getOsVersion());
        bstackOptions.setCapability("projectName", config.getProjectName());
        bstackOptions.setCapability("buildName", config.getBuildName());
        bstackOptions.setCapability("sessionName", "Mac Chrome Example");
        bstackOptions.setCapability("local", config.isBrowserstackLocal());

        caps.setCapability("bstack:options", bstackOptions);

        URL remoteUrl = new URL(
                String.format(
                        "https://%s:%s@hub-cloud.browserstack.com/wd/hub",
                        config.getUserName(),
                        config.getAccessKey()
                )
        );

        return DriverRequest.builder()
                .driverType(DriverType.REMOTE_WEB)
                .serverUrl(remoteUrl)
                .capabilities(caps)
                .description("BrowserStack Mac desktop web - " + platformLabel(platform))
                .build();
    }

    private static BrowserStackConfigDesktop loadConfig() {
        return YamlLoaderDesktop.load(YAML_PATH);
    }

    private static List<BrowserStackConfigDesktop.Platform> getPlatforms(BrowserStackConfigDesktop config) {
        if (config == null || config.getPlatforms() == null || config.getPlatforms().isEmpty()) {
            throw new IllegalStateException("No BrowserStack platforms were found in " + YAML_PATH);
        }
        return config.getPlatforms();
    }

    private static BrowserStackConfigDesktop.Platform getDefaultPlatform(BrowserStackConfigDesktop config) {
        return getPlatforms(config).get(0);
    }

    private static String platformLabel(BrowserStackConfigDesktop.Platform platform) {
        return String.format(
                "%s / %s / %s %s",
                platform.getOs(),
                platform.getOsVersion(),
                platform.getBrowserName(),
                platform.getBrowserVersion()
        );
    }
}
