package io.github.roberto22palomar.pepenium.tests.myProjectExample.local.app;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.flows.ExampleNavigationFlowAndroid;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages.BottomNavigationPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages.SearchPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsApp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.net.URL;
import java.time.Duration;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class ExampleAndroidLocalTest {

    private AppiumDriver driver;

    @BeforeAll
    public void setUp() throws Exception {
        String appiumUrl = envOrDefault("APPIUM_URL", "http://localhost:4723");
        String udid = envOrDefault("ANDROID_UDID", "emulator-5554");
        String deviceName = envOrDefault("ANDROID_DEVICE_NAME", "Android Device");

        UiAutomator2Options opts = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setDeviceName(deviceName)
                .setUdid(udid)
                .setNewCommandTimeout(Duration.ofSeconds(300))
                .setAutoGrantPermissions(true)
                .setNoReset(false)
                .setFullReset(false);

        // Optional app configuration (only if provided)
        // Tip: set APP_PATH to your local .apk path if you want Appium to install it.
        String appPath = System.getenv("APP_PATH");
        if (appPath != null && !appPath.isBlank()) {
            opts.setApp(stripQuotes(appPath));
            log.info("Using APP_PATH: {}", stripQuotes(appPath));
        }

        // Optional: if your app needs explicit package/activity, allow overriding via env vars
        String appPackage = System.getenv("APP_PACKAGE");
        String appActivity = System.getenv("APP_ACTIVITY");
        if (notBlank(appPackage) && notBlank(appActivity)) {
            opts.setAppPackage(appPackage);
            opts.setAppActivity(appActivity);
            log.info("Using APP_PACKAGE / APP_ACTIVITY: {} / {}", appPackage, appActivity);
        }

        log.info("Connecting to Appium at {}", appiumUrl);
        driver = new AndroidDriver(new URL(appiumUrl), opts);
        log.info("Driver initialized");
    }

    @Test
    public void basicNavigationFlow_shouldRunLocally() {
        ActionsApp actionsApp = new ActionsApp(driver);

        // Pages (example)
        SearchPage searchPage = new SearchPage(actionsApp);
        BottomNavigationPage bottomNavigationPage = new BottomNavigationPage(actionsApp);

        // Flow (example)
        ExampleNavigationFlowAndroid flow = new ExampleNavigationFlowAndroid(bottomNavigationPage, searchPage);
        flow.runBasicNavigationFlow();
    }

    @AfterAll
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            log.info("Driver closed");
        }
    }

    // -----------------------
    // Helpers
    // -----------------------

    private static String envOrDefault(String key, String defaultValue) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? defaultValue : v.trim();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String stripQuotes(String s) {
        return s == null ? null : s.replace("\"", "").trim();
    }
}
