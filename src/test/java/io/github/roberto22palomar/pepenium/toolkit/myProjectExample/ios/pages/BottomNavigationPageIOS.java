package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages;

import io.appium.java_client.AppiumBy;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsAppIOS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
@RequiredArgsConstructor
public class BottomNavigationPageIOS {

    // ============================================================
    // Generic locators (example)
    // ============================================================
    private final By tabOne = AppiumBy.accessibilityId("click-bottom-tab-1");
    private final By tabTwo = AppiumBy.accessibilityId("click-bottom-tab-2");
    private final By tabThree = AppiumBy.accessibilityId("click-bottom-tab-3");
    private final By tabHome = AppiumBy.accessibilityId("click_bottom_home");

    private final ActionsAppIOS actionsApp;

    // ============================================================
    // Actions
    // ============================================================

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
