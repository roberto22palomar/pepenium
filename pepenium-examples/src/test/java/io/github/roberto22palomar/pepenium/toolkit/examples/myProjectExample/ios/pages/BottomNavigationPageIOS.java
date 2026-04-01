package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.pages;

import io.appium.java_client.AppiumBy;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsAppIOS;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
public class BottomNavigationPageIOS {

    // ============================================================
    // Generic locators (example)
    // ============================================================
    private final By tabOne = AppiumBy.accessibilityId("click-bottom-tab-1");
    private final By tabTwo = AppiumBy.accessibilityId("click-bottom-tab-2");
    private final By tabThree = AppiumBy.accessibilityId("click-bottom-tab-3");
    private final By tabHome = AppiumBy.accessibilityId("click_bottom_home");

    @PepeniumInject
    private ActionsAppIOS actionsApp;

    @PepeniumInject
    private AssertionsAppIOS assertionsApp;

    // ============================================================
    // Actions
    // ============================================================

    public void waitUntilLoaded() {
        actionsApp.waitToBePresent(tabOne);
        actionsApp.waitToBePresent(tabTwo);
        actionsApp.waitToBePresent(tabThree);
        actionsApp.waitToBePresent(tabHome);
        actionsApp.takeScreenshotFast();
    }

    public void assertNavigationVisible() {
        assertionsApp.assertVisible(tabOne);
        assertionsApp.assertVisible(tabTwo);
        assertionsApp.assertVisible(tabThree);
        assertionsApp.assertVisible(tabHome);
    }

    public void assertHomeVisible() {
        assertionsApp.assertVisible(tabHome);
    }

    public void assertTabThreeVisible() {
        assertionsApp.assertVisible(tabThree);
    }

    public void openTabOne() {
        log.info("Opening bottom navigation: Tab One");
        actionsApp.click(tabOne);
        actionsApp.takeScreenshot();
    }

    public void openTabTwo() {
        log.info("Opening bottom navigation: Tab Two");
        actionsApp.click(tabTwo);
        actionsApp.takeScreenshot();
    }

    public void openTabThree() {
        log.info("Opening bottom navigation: Tab Three");
        actionsApp.click(tabThree);
        actionsApp.takeScreenshot();
    }

    public void openHome() {
        log.info("Opening bottom navigation: Home");
        actionsApp.click(tabHome);
        actionsApp.takeScreenshot();
    }
}
