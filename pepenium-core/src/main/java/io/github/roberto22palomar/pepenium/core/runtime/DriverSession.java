package io.github.roberto22palomar.pepenium.core.runtime;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import lombok.Getter;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "The owned Appium service is a runtime handle that cannot be defensively copied."
)
public class DriverSession implements AutoCloseable {
    private final WebDriver driver;
    private final DriverRequest request;
    private final AppiumDriverLocalService ownedService;
    private final AtomicBoolean closed = new AtomicBoolean();

    public DriverSession(WebDriver driver, DriverRequest request) {
        this.driver = driver;
        this.request = request;
        this.ownedService = request.getOwnedService();
    }

    public AppiumDriver getAppiumDriver() {
        if (driver instanceof AppiumDriver) {
            return (AppiumDriver) driver;
        }
        throw new IllegalStateException("Current session is not backed by Appium: " + request.getDriverType());
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        RuntimeException failure = null;
        try {
            if (driver != null) {
                driver.quit();
            }
        } catch (RuntimeException error) {
            failure = error;
        }
        try {
            if (ownedService != null) {
                ownedService.stop();
            }
        } catch (RuntimeException error) {
            if (failure == null) {
                failure = error;
            } else {
                failure.addSuppressed(error);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}
