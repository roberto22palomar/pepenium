package io.github.roberto22palomar.pepenium.toolkit.assertions;

import io.appium.java_client.AppiumDriver;

public class AssertionsApp extends PlatformAssertions implements MobileAssertions {

    public AssertionsApp(AppiumDriver driver) {
        super(driver, "Android");
    }

    @Override
    public AppiumDriver getDriver() {
        return (AppiumDriver) driver;
    }
}
