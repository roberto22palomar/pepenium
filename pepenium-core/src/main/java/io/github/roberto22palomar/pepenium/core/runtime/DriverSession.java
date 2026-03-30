package io.github.roberto22palomar.pepenium.core.runtime;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import lombok.Getter;
import org.openqa.selenium.WebDriver;

@Getter
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "The owned Appium service is a runtime handle that cannot be defensively copied."
)
public class DriverSession implements AutoCloseable {
    private final WebDriver driver;
    private final DriverRequest request;
    private final AppiumDriverLocalService ownedService;

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
        if (driver != null) {
            driver.quit();
        }
        if (ownedService != null) {
            ownedService.stop();
        }
    }
}
