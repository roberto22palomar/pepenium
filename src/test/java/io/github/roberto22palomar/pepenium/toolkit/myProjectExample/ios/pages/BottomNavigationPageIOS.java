package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages;

import io.appium.java_client.AppiumBy;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.utils.AssertionsAppIOS;
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
    private final AssertionsAppIOS assertionsAppIOS;

    // ============================================================
    // Assertions and Actions
    // ============================================================

    public void assertBottomNavigationLoaded() {
        log.info("Asserting bottom navigation is loaded (iOS)");
        assertionsAppIOS.assertVisible(tabOne);
        assertionsAppIOS.assertVisible(tabTwo);
        assertionsAppIOS.assertVisible(tabHome);
    }

    public void openTabOne() {
        log.info("Opening bottom navigation: Tab One");
        assertionsAppIOS.assertClickable(tabOne);
        actionsApp.click(tabOne);
        assertionsAppIOS.assertVisible(tabOne);
        actionsApp.takeScreenshot();
    }

    public void openTabTwo() {
        log.info("Opening bottom navigation: Tab Two");
        assertionsAppIOS.assertClickable(tabTwo);
        actionsApp.click(tabTwo);
        assertionsAppIOS.assertVisible(tabTwo);
        actionsApp.takeScreenshot();
    }

    public void openTabThree() {
        log.info("Opening bottom navigation: Tab Three");
        assertionsAppIOS.assertClickable(tabThree);
        actionsApp.click(tabThree);
        assertionsAppIOS.assertVisible(tabThree);
        actionsApp.takeScreenshot();
    }

    public void openHome() {
        log.info("Opening bottom navigation: Home");
        assertionsAppIOS.assertClickable(tabHome);
        actionsApp.click(tabHome);
        assertionsAppIOS.assertVisible(tabHome);
        actionsApp.takeScreenshot();
    }
}
