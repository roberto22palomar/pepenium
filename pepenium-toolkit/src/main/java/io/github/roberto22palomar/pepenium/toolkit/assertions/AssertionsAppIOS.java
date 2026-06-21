package io.github.roberto22palomar.pepenium.toolkit.assertions;

import io.appium.java_client.AppiumDriver;

public class AssertionsAppIOS extends PlatformAssertions implements MobileAssertions {

    public AssertionsAppIOS(AppiumDriver driver) {
        super(driver, "iOS");
    }

    @Override
    public AppiumDriver getDriver() {
        return (AppiumDriver) driver;
    }
}
