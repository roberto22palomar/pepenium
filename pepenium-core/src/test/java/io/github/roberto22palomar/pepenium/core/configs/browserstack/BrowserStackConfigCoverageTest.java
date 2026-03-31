package io.github.roberto22palomar.pepenium.core.configs.browserstack;

import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfigDesktop;
import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfigMobile;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.android.AndroidConfigBS;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.android.AndroidWebConfigBS;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.desktop.MacWebConfigBS;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.desktop.WindowsWebConfigBS;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.ios.IOSConfigBS;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.ios.IOSWebConfigBS;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Capabilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BrowserStackConfigCoverageTest {

    @Test
    void androidNativePlatformsExposeExampleCatalog() {
        assertEquals(3L, AndroidConfigBS.platforms().count());
    }

    @Test
    void androidNativeCreateRequestUsesExampleFallback() throws Exception {
        DriverRequest request = new AndroidConfigBS().createRequest();

        assertEquals(DriverType.ANDROID_APPIUM, request.getDriverType());
        assertBrowserStackRemoteUrl(request, "YOUR_BROWSERSTACK_USERNAME", "YOUR_BROWSERSTACK_ACCESS_KEY");
        assertEquals("BrowserStack Android native app - android / Motorola Moto G9 Play / 10.0", request.getDescription());
        assertEquals("android", lowerCaseCapability(request, "platformName"));
        assertEquals("UiAutomator2", request.getCapabilities().getCapability("appium:automationName"));
        assertEquals("Motorola Moto G9 Play", request.getCapabilities().getCapability("appium:deviceName"));
        assertEquals("10.0", String.valueOf(request.getCapabilities().getCapability("appium:platformVersion")));
        assertEquals("bs://YOUR_UPLOADED_APP_ID", request.getCapabilities().getCapability("appium:app"));
        assertEquals("Pepenium-Android", bstackOptions(request).getCapability("projectName"));
        assertEquals("pepenium-android-build-1", bstackOptions(request).getCapability("buildName"));
        assertEquals(Boolean.TRUE, bstackOptions(request).getCapability("appProfiling"));
        assertEquals(Boolean.TRUE, bstackOptions(request).getCapability("networkLogs"));
        assertEquals(Boolean.FALSE, request.getCapabilities().getCapability("browserstack.local"));
    }

    @Test
    void androidNativeCreateRequestSupportsProvidedPlatformOverride() throws Exception {
        BrowserStackConfig.Platform platform = BrowserStackConfig.Platform.builder()
                .platformName("android")
                .deviceName("Pixel 9")
                .platformVersion("15")
                .build();

        DriverRequest request = new AndroidConfigBS(platform).createRequest();

        assertEquals("BrowserStack Android native app - android / Pixel 9 / 15", request.getDescription());
        assertEquals("Pixel 9", request.getCapabilities().getCapability("appium:deviceName"));
        assertEquals("15", String.valueOf(request.getCapabilities().getCapability("appium:platformVersion")));
    }

    @Test
    void iosNativePlatformsExposeExampleCatalog() {
        assertEquals(1L, IOSConfigBS.platforms().count());
    }

    @Test
    void iosNativeCreateRequestUsesExampleFallback() throws Exception {
        DriverRequest request = new IOSConfigBS().createRequest();

        assertEquals(DriverType.IOS_APPIUM, request.getDriverType());
        assertBrowserStackRemoteUrl(request, "YOUR_BROWSERSTACK_USERNAME", "YOUR_BROWSERSTACK_ACCESS_KEY");
        assertEquals("BrowserStack iOS native app - ios / iPhone 14 Pro Max / 16", request.getDescription());
        assertEquals("ios", lowerCaseCapability(request, "platformName"));
        assertEquals("iPhone 14 Pro Max", request.getCapabilities().getCapability("appium:deviceName"));
        assertEquals("16", String.valueOf(request.getCapabilities().getCapability("appium:platformVersion")));
        assertEquals("bs://YOUR_UPLOADED_IOS_APP_ID", request.getCapabilities().getCapability("appium:app"));
        assertEquals("Pepenium-iOS", bstackOptions(request).getCapability("projectName"));
        assertEquals("pepenium-ios-build-1", bstackOptions(request).getCapability("buildName"));
    }

    @Test
    void androidWebCreateRequestUsesExampleFallback() throws Exception {
        DriverRequest request = new AndroidWebConfigBS().createRequest();

        assertEquals(1L, AndroidWebConfigBS.platforms().count());
        assertEquals(DriverType.ANDROID_APPIUM, request.getDriverType());
        assertBrowserStackRemoteUrl(request, "YOUR_BROWSERSTACK_USERNAME", "YOUR_BROWSERSTACK_ACCESS_KEY");
        assertEquals("BrowserStack Android web - Oppo A96 / 11.0 / chrome", request.getDescription());
        assertEquals("android", lowerCaseCapability(request, "platformName"));
        assertEquals("chrome", request.getCapabilities().getCapability("browserName"));
        assertEquals("Oppo A96", bstackOptions(request).getCapability("deviceName"));
        assertEquals("11.0", String.valueOf(bstackOptions(request).getCapability("osVersion")));
        assertEquals("portrait", bstackOptions(request).getCapability("deviceOrientation"));
        assertEquals(Boolean.TRUE, bstackOptions(request).getCapability("appProfiling"));
    }

    @Test
    void iosWebCreateRequestSupportsProvidedPlatformOverride() throws Exception {
        BrowserStackConfigMobile.Platform platform = BrowserStackConfigMobile.Platform.builder()
                .deviceName("iPhone 16")
                .osVersion("18")
                .browserName("Safari")
                .deviceOrientation("landscape")
                .build();

        DriverRequest request = new IOSWebConfigBS(platform).createRequest();

        assertEquals(1L, IOSWebConfigBS.platforms().count());
        assertEquals("BrowserStack iOS web - iPhone 16 / 18 / Safari", request.getDescription());
        assertEquals("ios", lowerCaseCapability(request, "platformName"));
        assertEquals("Safari", request.getCapabilities().getCapability("browserName"));
        assertEquals("iPhone 16", bstackOptions(request).getCapability("deviceName"));
        assertEquals("18", String.valueOf(bstackOptions(request).getCapability("osVersion")));
        assertEquals("landscape", bstackOptions(request).getCapability("deviceOrientation"));
    }

    @Test
    void windowsDesktopCreateRequestUsesExampleFallback() throws Exception {
        DriverRequest request = new WindowsWebConfigBS().createRequest();

        assertEquals(1L, WindowsWebConfigBS.platforms().count());
        assertEquals(DriverType.REMOTE_WEB, request.getDriverType());
        assertBrowserStackRemoteUrl(request, "user", "1234");
        assertEquals("BrowserStack Windows desktop web - Windows / 11 / Chrome latest", request.getDescription());
        assertEquals("Chrome", request.getCapabilities().getCapability("browserName"));
        assertEquals("latest", request.getCapabilities().getCapability("browserVersion"));
        assertEquals("Windows", bstackOptions(request).getCapability("os"));
        assertEquals("11", String.valueOf(bstackOptions(request).getCapability("osVersion")));
        assertEquals("BrowserStack Sample", bstackOptions(request).getCapability("projectName"));
        assertEquals(Boolean.TRUE, bstackOptions(request).getCapability("networkLogs"));
    }

    @Test
    void macDesktopCreateRequestUsesDedicatedExampleFallback() throws Exception {
        DriverRequest request = new MacWebConfigBS().createRequest();

        assertEquals(1L, MacWebConfigBS.platforms().count());
        assertEquals(DriverType.REMOTE_WEB, request.getDriverType());
        assertBrowserStackRemoteUrl(request, "user", "1234");
        assertEquals("BrowserStack Mac desktop web - OS X / Sonoma / Chrome latest", request.getDescription());
        assertEquals("Chrome", request.getCapabilities().getCapability("browserName"));
        assertEquals("latest", request.getCapabilities().getCapability("browserVersion"));
        assertEquals("OS X", bstackOptions(request).getCapability("os"));
        assertEquals("Sonoma", String.valueOf(bstackOptions(request).getCapability("osVersion")));
        assertEquals("BrowserStack Mac Sample", bstackOptions(request).getCapability("projectName"));
        assertEquals("Mac Chrome Example", bstackOptions(request).getCapability("sessionName"));
    }

    private static Capabilities bstackOptions(DriverRequest request) {
        return assertInstanceOf(Capabilities.class, request.getCapabilities().getCapability("bstack:options"));
    }

    private static void assertBrowserStackRemoteUrl(DriverRequest request, String expectedUser, String expectedKey) {
        assertEquals("https", request.getServerUrl().getProtocol());
        assertEquals("hub-cloud.browserstack.com", request.getServerUrl().getHost());
        assertEquals("/wd/hub", request.getServerUrl().getPath());

        String userInfo = request.getServerUrl().getUserInfo();
        assertNotNull(userInfo);

        String[] userInfoParts = userInfo.split(":", 2);
        assertEquals(2, userInfoParts.length);
        assertEquals(expectedUser, userInfoParts[0]);
        assertEquals(expectedKey, userInfoParts[1]);
    }

    private static String lowerCaseCapability(DriverRequest request, String capabilityName) {
        Object value = request.getCapabilities().getCapability(capabilityName);
        return value == null ? null : String.valueOf(value).toLowerCase();
    }
}
