package io.github.roberto22palomar.pepenium.toolkit.assertions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.appium.java_client.AppiumDriver;

public class AssertionsAppIOS extends PlatformAssertions implements MobileAssertions {

    public AssertionsAppIOS(AppiumDriver driver) {
        super(driver, "iOS");
    }

    @Override
    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
            justification = "AssertionsAppIOS intentionally exposes the active AppiumDriver for advanced consumer workflows."
    )
    public AppiumDriver getDriver() {
        return (AppiumDriver) driver;
    }
}
