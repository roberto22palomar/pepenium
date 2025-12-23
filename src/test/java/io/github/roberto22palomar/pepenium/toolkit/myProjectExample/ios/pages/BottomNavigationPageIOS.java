package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages;

import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsAppIOS;
import io.appium.java_client.AppiumBy;
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
        actionsApp.hacerClick(tabOne);
        actionsApp.hacerCapturaPantalla();
    }

    public void openTabTwo() {
        log.info("Opening bottom navigation: Tab Two");
        actionsApp.hacerClick(tabTwo);
        actionsApp.hacerCapturaPantalla();
    }

    public void openTabThree() {
        log.info("Opening bottom navigation: Tab Three");
        actionsApp.hacerClick(tabThree);
        actionsApp.hacerCapturaPantalla();
    }

    public void openHome() {
        log.info("Opening bottom navigation: Home");
        actionsApp.hacerClick(tabHome);
        actionsApp.hacerCapturaPantalla();
    }
}
