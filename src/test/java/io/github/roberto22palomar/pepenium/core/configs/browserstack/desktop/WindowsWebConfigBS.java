package io.github.roberto22palomar.pepenium.core.configs.browserstack.desktop;

import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfigDesktop;
import io.github.roberto22palomar.pepenium.toolkit.utils.YamlLoaderDesktop;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

public class WindowsWebConfigBS {

    private final BrowserStackConfigDesktop config;

    // Loads BrowserStack desktop settings from a YAML file (browser/OS/build metadata and credentials).
    public WindowsWebConfigBS() {
        config = YamlLoaderDesktop.load("src/test/resources/browserstack.yml");
    }

    public WebDriver createDriver() throws Exception {
        BrowserStackConfigDesktop.Platform platform = config.getPlatforms().get(0);

        // Base Selenium capabilities (browser + version).
        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("browserName", platform.getBrowserName());
        caps.setCapability("browserVersion", platform.getBrowserVersion());

        // BrowserStack-specific options (OS selection, build metadata, logs).
        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("os", platform.getOs());
        bstackOptions.setCapability("osVersion", platform.getOsVersion());
        bstackOptions.setCapability("projectName", config.getProjectName());
        bstackOptions.setCapability("buildName", config.getBuildName());
        // Human-friendly name shown in the BrowserStack dashboard.
        bstackOptions.setCapability("sessionName", "Windows Browser Example - Pepenium");
        bstackOptions.setCapability("local", config.isBrowserstackLocal());
        bstackOptions.setCapability("networkLogs", true);
        // bstackOptions.setCapability("performance", "assert"); // Optional

        caps.setCapability("bstack:options", bstackOptions);

        // Remote hub URL with BrowserStack credentials.
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
