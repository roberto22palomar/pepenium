package io.github.roberto22palomar.pepenium.toolkit.utils;

import io.appium.java_client.AppiumDriver;

import java.time.Duration;
import java.util.Objects;

public class AssertionsAppIOS extends BaseAssertionsMobile {

    private final ActionsAppIOS actionsAppIOS;

    public AssertionsAppIOS(AppiumDriver driver) {
        this(driver, new ActionsAppIOS(driver));
    }

    public AssertionsAppIOS(AppiumDriver driver, ActionsAppIOS actionsAppIOS) {
        this(driver, actionsAppIOS, Duration.ofSeconds(10L));
    }

    public AssertionsAppIOS(AppiumDriver driver, ActionsAppIOS actionsAppIOS, Duration defaultTimeout) {
        super(driver, defaultTimeout);
        this.actionsAppIOS = Objects.requireNonNull(actionsAppIOS, "actionsAppIOS cannot be null");
    }

    @Override
    protected boolean isScreenStable() {
        return actionsAppIOS.waitStableScreen();
    }

    @Override
    protected String safeScreenshot() {
        try {
            return actionsAppIOS.takeScreenshot();
        } catch (Exception e) {
            return null;
        }
    }
}
