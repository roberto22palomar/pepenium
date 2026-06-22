package io.github.roberto22palomar.pepenium.core.runtime;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.observability.CapabilitiesSummary;
import io.github.roberto22palomar.pepenium.core.observability.LoggingContext;
import io.github.roberto22palomar.pepenium.core.observability.PepeniumBanner;
import io.github.roberto22palomar.pepenium.core.observability.SensitiveDataSanitizer;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.http.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;
import java.util.function.BiConsumer;

public class DefaultDriverSessionFactory implements DriverSessionFactory {

    private static final Logger log = LoggerFactory.getLogger(DefaultDriverSessionFactory.class);
    private final BiConsumer<DriverRequest, Duration> endpointPreflight;

    public DefaultDriverSessionFactory() {
        this(DriverEndpointPreflight::verifyLocalEndpoint);
    }

    DefaultDriverSessionFactory(BiConsumer<DriverRequest, Duration> endpointPreflight) {
        this.endpointPreflight = endpointPreflight;
    }

    @Override
    public DriverSession create(DriverRequest request) throws Exception {
        try {
            validateRequest(request);
            Duration connectTimeout = SessionTimeouts.connectTimeout();
            Duration commandTimeout = SessionTimeouts.commandTimeout();
            endpointPreflight.accept(request, connectTimeout);
            PepeniumBanner.print(request);
            log.info("Creating driver session: description='{}', capabilities={}",
                    request.getDescription(),
                    CapabilitiesSummary.summarize(request.getCapabilities()));
            if (request.getServerUrl() != null) {
                log.info("Driver server: {}", SensitiveDataSanitizer.sanitizeServerUrl(request.getServerUrl()));
            }
            log.info("Effective capabilities: {}", CapabilitiesSummary.describe(request.getCapabilities()));
            log.info("Session transport timeouts: connect={}, command={}", connectTimeout, commandTimeout);

            WebDriver driver;
            switch (request.getDriverType()) {
                case ANDROID_APPIUM:
                    driver = new AndroidDriver(commandExecutor(request, connectTimeout, commandTimeout),
                            requireCapabilities(request));
                    break;
                case IOS_APPIUM:
                    driver = new IOSDriver(commandExecutor(request, connectTimeout, commandTimeout),
                            requireCapabilities(request));
                    break;
                case REMOTE_WEB:
                    driver = new RemoteWebDriver(commandExecutor(request, connectTimeout, commandTimeout),
                            requireCapabilities(request));
                    break;
                case LOCAL_CHROME:
                    driver = new ChromeDriver(resolveChromeOptions(request.getCapabilities()));
                    break;
                case LOCAL_FIREFOX:
                    driver = new FirefoxDriver(resolveFirefoxOptions(request.getCapabilities()));
                    break;
                case LOCAL_EDGE:
                    driver = new EdgeDriver(resolveEdgeOptions(request.getCapabilities()));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported driver type: " + request.getDriverType());
            }

            String sessionId = String.valueOf(((RemoteWebDriver) driver).getSessionId());
            LoggingContext.setSessionId(sessionId);
            log.info("Driver session created successfully");
            return new DriverSession(driver, request);
        } catch (Exception error) {
            if (request != null && request.getOwnedService() != null) {
                try {
                    request.getOwnedService().stop();
                } catch (RuntimeException stopError) {
                    error.addSuppressed(stopError);
                }
            }
            throw error;
        }
    }

    private void validateRequest(DriverRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Driver request must not be null");
        }
        if (request.getDriverType() == null) {
            throw new IllegalStateException("Driver request does not provide a driver type: " + request.getDescription());
        }
        switch (request.getDriverType()) {
            case ANDROID_APPIUM:
            case IOS_APPIUM:
            case REMOTE_WEB:
                requireServerUrl(request);
                requireCapabilities(request);
                break;
            default:
                break;
        }
    }

    private HttpCommandExecutor commandExecutor(DriverRequest request,
                                                Duration connectTimeout,
                                                Duration commandTimeout) {
        ClientConfig clientConfig = ClientConfig.defaultConfig()
                .baseUrl(requireServerUrl(request))
                .connectionTimeout(connectTimeout)
                .readTimeout(commandTimeout);
        return new HttpCommandExecutor(clientConfig);
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

        return mergeCapabilities(capabilities, new ChromeOptions());
    }

    private FirefoxOptions resolveFirefoxOptions(Capabilities capabilities) {
        if (capabilities instanceof FirefoxOptions) {
            return (FirefoxOptions) capabilities;
        }

        return mergeCapabilities(capabilities, new FirefoxOptions());
    }

    private EdgeOptions resolveEdgeOptions(Capabilities capabilities) {
        if (capabilities instanceof EdgeOptions) {
            return (EdgeOptions) capabilities;
        }

        return mergeCapabilities(capabilities, new EdgeOptions());
    }

    private <T extends MutableCapabilities> T mergeCapabilities(Capabilities capabilities, T options) {
        if (capabilities != null) {
            @SuppressWarnings("unchecked")
            T mergedOptions = (T) options.merge(capabilities);
            return mergedOptions;
        }
        return options;
    }
}
