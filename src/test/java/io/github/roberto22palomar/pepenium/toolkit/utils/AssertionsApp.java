package io.github.roberto22palomar.pepenium.toolkit.utils;

import io.appium.java_client.AppiumDriver;

import java.time.Duration;
import java.util.Objects;

public class AssertionsApp extends BaseAssertionsMobile {

    private final ActionsApp actionsApp;

    public AssertionsApp(AppiumDriver driver) {
        this(driver, new ActionsApp(driver));
    }

    public AssertionsApp(AppiumDriver driver, ActionsApp actionsApp) {
        this(driver, actionsApp, Duration.ofSeconds(6L));
    }

    public AssertionsApp(AppiumDriver driver, ActionsApp actionsApp, Duration defaultTimeout) {
        super(driver, defaultTimeout);
        this.actionsApp = Objects.requireNonNull(actionsApp, "actionsApp cannot be null");
    }

    @Override
    protected boolean isScreenStable() {
        return actionsApp.waitStableScreen();
    }

    @Override
    protected String safeScreenshot() {
        try {
            return actionsApp.takeScreenshot();
        } catch (Exception e) {
            return null;
        }
    }
}
