package io.github.roberto22palomar.pepenium.core.configs.browserstack.android;

import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfigMobile;
import io.github.roberto22palomar.pepenium.toolkit.utils.YamlLoaderMobile;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.openqa.selenium.MutableCapabilities;

import java.net.URL;

public class AndroidWebConfigBS implements DriverConfig {

    private final BrowserStackConfigMobile config;

    public AndroidWebConfigBS() {
        config = YamlLoaderMobile.load("src/test/resources/browserstackAndroidWEB.yml");
    }

    @Override
    public AppiumDriver createDriver(AppiumDriverLocalService service) throws Exception {
        BrowserStackConfigMobile.Platform p = config.getPlatforms().get(0);

        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("browserName", "Chrome");

        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("deviceName", p.getDeviceName());
        //bstackOptions.setCapability("platformVersion", p.getPlatformVersion());
        bstackOptions.setCapability("projectName", config.getProjectName());
        bstackOptions.setCapability("buildName", config.getBuildName());
        bstackOptions.setCapability("appProfiling", true);
        bstackOptions.setCapability("networkLogs", true);
        bstackOptions.setCapability("local", config.isBrowserstackLocal());

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
        return null; // no service local
    }
}
