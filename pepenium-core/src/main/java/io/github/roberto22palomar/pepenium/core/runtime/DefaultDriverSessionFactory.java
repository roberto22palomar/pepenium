package io.github.roberto22palomar.pepenium.core.runtime;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.observability.CapabilitiesSummary;
import io.github.roberto22palomar.pepenium.core.observability.LoggingContext;
import io.github.roberto22palomar.pepenium.core.observability.PepeniumBanner;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class DefaultDriverSessionFactory implements DriverSessionFactory {

    private static final Logger log = LoggerFactory.getLogger(DefaultDriverSessionFactory.class);

    @Override
    public DriverSession create(DriverRequest request) throws Exception {
        PepeniumBanner.print(request);
        log.info("Creating driver session: description='{}', capabilities={}",
                request.getDescription(),
                CapabilitiesSummary.summarize(request.getCapabilities()));
        WebDriver driver;

        switch (request.getDriverType()) {
            case ANDROID_APPIUM:
                driver = new AndroidDriver(requireServerUrl(request), requireCapabilities(request));
                break;
            case IOS_APPIUM:
                driver = new IOSDriver(requireServerUrl(request), requireCapabilities(request));
                break;
            case REMOTE_WEB:
                driver = new RemoteWebDriver(requireServerUrl(request), requireCapabilities(request));
                break;
            case LOCAL_CHROME:
                driver = new ChromeDriver(resolveChromeOptions(request.getCapabilities()));
                break;
            default:
                throw new IllegalArgumentException("Unsupported driver type: " + request.getDriverType());
        }

        if (driver instanceof RemoteWebDriver) {
            String sessionId = String.valueOf(((RemoteWebDriver) driver).getSessionId());
            LoggingContext.setSessionId(sessionId);
            log.info("Driver session created successfully");
        } else {
            log.info("Driver session created successfully");
        }

        return new DriverSession(driver, request);
    }

    private URL requireServerUrl(DriverRequest request) {
        if (request.getServerUrl() == null) {
            throw new IllegalStateException("Driver request does not provide a server URL: " + request.getDescription());
        }
        return request.getServerUrl();
    }

    private Capabilities requireCapabilities(DriverRequest request) {
        if (request.getCapabilities() == null) {
            throw new IllegalStateException("Driver request does not provide capabilities: " + request.getDescription());
        }
        return request.getCapabilities();
    }

    private ChromeOptions resolveChromeOptions(Capabilities capabilities) {
        if (capabilities instanceof ChromeOptions) {
            return (ChromeOptions) capabilities;
        }

        ChromeOptions options = new ChromeOptions();
        if (capabilities != null) {
            options.merge(capabilities);
        }
        return options;
    }
}
