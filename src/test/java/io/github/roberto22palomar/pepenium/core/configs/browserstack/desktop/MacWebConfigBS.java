package io.github.roberto22palomar.pepenium.core.configs.browserstack.desktop;

import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfigDesktop;
import io.github.roberto22palomar.pepenium.toolkit.utils.YamlLoaderDesktop;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

public class MacWebConfigBS {

    private final BrowserStackConfigDesktop config;

    public MacWebConfigBS() {
        config = YamlLoaderDesktop.load("src/test/resources/browserstackMac.yml");
    }

    public WebDriver createDriver() throws Exception {
        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("browserName", config.getPlatforms().get(0).getBrowserName());
        caps.setCapability("browserVersion", config.getPlatforms().get(0).getBrowserVersion());

        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("os", config.getPlatforms().get(0).getOs());
        bstackOptions.setCapability("osVersion", config.getPlatforms().get(0).getOsVersion());
        bstackOptions.setCapability("projectName", config.getProjectName());
        bstackOptions.setCapability("buildName", config.getBuildName());
        bstackOptions.setCapability("sessionName", "Prueba Mac Chrome");
        bstackOptions.setCapability("local", config.isBrowserstackLocal());

        caps.setCapability("bstack:options", bstackOptions);

        URL remoteUrl = new URL(
                String.format(
                        "https://%s:%s@hub-cloud.browserstack.com/wd/hub",
                        config.getUserName(),
                        config.getAccessKey()
                )
        );

        return new RemoteWebDriver(remoteUrl, caps);
    }
}
