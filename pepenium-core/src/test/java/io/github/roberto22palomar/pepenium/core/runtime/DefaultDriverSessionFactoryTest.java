package io.github.roberto22palomar.pepenium.core.runtime;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.observability.LoggingContext;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class DefaultDriverSessionFactoryTest {

    @AfterEach
    void tearDown() {
        LoggingContext.clearAll();
    }

    @Test
    void createLocalChromeBuildsDriverSessionWithMergedOptions() throws Exception {
        DefaultDriverSessionFactory factory = new DefaultDriverSessionFactory();
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("browserName", "chrome");
        capabilities.setCapability("acceptInsecureCerts", true);
        DriverRequest request = DriverRequest.builder()
                .driverType(DriverType.LOCAL_CHROME)
                .description("local chrome")
                .target(TestTarget.WEB_DESKTOP)
                .capabilities(capabilities)
                .build();
        AtomicReference<ChromeOptions> constructedOptions = new AtomicReference<>();

        try (MockedConstruction<ChromeDriver> ignored = mockConstruction(ChromeDriver.class, (mock, context) -> {
            constructedOptions.set((ChromeOptions) context.arguments().get(0));
            when(mock.getSessionId()).thenReturn(new SessionId("chrome-session-123456"));
        })) {
            DriverSession session = factory.create(request);

            assertSame(request, session.getRequest());
            assertNotNull(constructedOptions.get());
            assertEquals("chrome", constructedOptions.get().getBrowserName());
            assertEquals("chrome-session-123456", ThreadContext.get("sessionId"));
            assertEquals("chrome-s", ThreadContext.get("sessionShort"));
        }
    }

    @Test
    void createRemoteWebRequiresServerUrl() {
        DefaultDriverSessionFactory factory = new DefaultDriverSessionFactory();
        DriverRequest request = DriverRequest.builder()
                .driverType(DriverType.REMOTE_WEB)
                .description("remote web")
                .target(TestTarget.WEB_DESKTOP)
                .capabilities(new MutableCapabilities())
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factory.create(request));

        assertEquals("Driver request does not provide a server URL: remote web", exception.getMessage());
    }

    @Test
    void createRemoteWebRequiresCapabilities() throws Exception {
        DefaultDriverSessionFactory factory = new DefaultDriverSessionFactory();
        DriverRequest request = DriverRequest.builder()
                .driverType(DriverType.REMOTE_WEB)
                .description("remote web")
                .target(TestTarget.WEB_DESKTOP)
                .serverUrl(new URL("https://grid.example.test/wd/hub"))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factory.create(request));

        assertEquals("Driver request does not provide capabilities: remote web", exception.getMessage());
    }

    @Test
    void createRemoteWebBuildsRemoteDriverSession() throws Exception {
        DefaultDriverSessionFactory factory = new DefaultDriverSessionFactory();
        URL serverUrl = new URL("https://grid.example.test/wd/hub");
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("browserName", "firefox");
        DriverRequest request = DriverRequest.builder()
                .driverType(DriverType.REMOTE_WEB)
                .description("remote web")
                .target(TestTarget.WEB_DESKTOP)
                .serverUrl(serverUrl)
                .capabilities(capabilities)
                .build();

        try (MockedConstruction<RemoteWebDriver> construction = mockConstruction(RemoteWebDriver.class, (mock, context) -> {
            assertSame(serverUrl, context.arguments().get(0));
            assertSame(capabilities, context.arguments().get(1));
            when(mock.getSessionId()).thenReturn(new SessionId("remote-session"));
        })) {
            DriverSession session = factory.create(request);

            assertSame(construction.constructed().get(0), session.getDriver());
            assertEquals("remote-session", ThreadContext.get("sessionId"));
        }
    }

    @Test
    void createAndroidAppiumBuildsAndroidDriverSession() throws Exception {
        DefaultDriverSessionFactory factory = new DefaultDriverSessionFactory();
        URL serverUrl = new URL("http://127.0.0.1:4723");
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("platformName", "Android");
        DriverRequest request = DriverRequest.builder()
                .driverType(DriverType.ANDROID_APPIUM)
                .description("android app")
                .target(TestTarget.ANDROID_NATIVE)
                .serverUrl(serverUrl)
                .capabilities(capabilities)
                .build();

        try (MockedConstruction<AndroidDriver> construction = mockConstruction(AndroidDriver.class, (mock, context) -> {
            assertSame(serverUrl, context.arguments().get(0));
            assertSame(capabilities, context.arguments().get(1));
            when(mock.getSessionId()).thenReturn(new SessionId("android-session"));
        })) {
            DriverSession session = factory.create(request);

            assertSame(construction.constructed().get(0), session.getDriver());
            assertEquals("android-session", ThreadContext.get("sessionId"));
        }
    }

    @Test
    void createIosAppiumBuildsIosDriverSession() throws Exception {
        DefaultDriverSessionFactory factory = new DefaultDriverSessionFactory();
        URL serverUrl = new URL("http://127.0.0.1:4723");
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("platformName", "iOS");
        DriverRequest request = DriverRequest.builder()
                .driverType(DriverType.IOS_APPIUM)
                .description("ios app")
                .target(TestTarget.IOS_NATIVE)
                .serverUrl(serverUrl)
                .capabilities(capabilities)
                .build();

        try (MockedConstruction<IOSDriver> construction = mockConstruction(IOSDriver.class, (mock, context) -> {
            assertSame(serverUrl, context.arguments().get(0));
            assertSame(capabilities, context.arguments().get(1));
            when(mock.getSessionId()).thenReturn(new SessionId("ios-session"));
        })) {
            DriverSession session = factory.create(request);

            assertSame(construction.constructed().get(0), session.getDriver());
            assertEquals("ios-session", ThreadContext.get("sessionId"));
        }
    }
}
