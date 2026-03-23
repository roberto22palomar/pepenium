package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages;

import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsApp;
import io.github.roberto22palomar.pepenium.toolkit.utils.AssertionsApp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
@RequiredArgsConstructor
public class BottomNavigationPage {

    // ============================================================
    // Generic locators (example)
    // ============================================================
    private final By tabOne = By.xpath("//*[@resource-id='click-bottom-tab-1']");
    private final By tabTwo = By.xpath("//*[@resource-id='click-bottom-tab-2']");
    private final By tabThree = By.xpath("//*[@resource-id='click-bottom-tab-3']");
    private final By tabHome = By.xpath("//*[@resource-id='click_bottom_home']");

    private final ActionsApp actionsApp;
    private final AssertionsApp assertionsApp;

    // ============================================================
    // Assertions and Actions
    // ============================================================

    public void assertBottomNavigationLoaded() {
        log.info("Asserting bottom navigation is loaded (Android)");
        assertionsApp.assertVisible(tabOne);
        assertionsApp.assertVisible(tabTwo);
        assertionsApp.assertVisible(tabHome);
    }

    public void openTabOne() {
        log.info("Opening bottom navigation: Tab One");
        assertionsApp.assertClickable(tabOne);
        actionsApp.makeClick(tabOne);
        assertionsApp.assertVisible(tabOne);
        actionsApp.takeScreenshot();
    }

    public void openTabTwo() {
        log.info("Opening bottom navigation: Tab Two");
        assertionsApp.assertClickable(tabTwo);
        actionsApp.makeClick(tabTwo);
        assertionsApp.assertVisible(tabTwo);
        actionsApp.takeScreenshot();
    }

    public void openTabThree() {
        log.info("Opening bottom navigation: Tab Three");
        assertionsApp.assertClickable(tabThree);
        actionsApp.makeClick(tabThree);
        assertionsApp.assertVisible(tabThree);
        actionsApp.takeScreenshot();
    }

    public void openHome() {
        log.info("Opening bottom navigation: Home");
        assertionsApp.assertClickable(tabHome);
        actionsApp.makeClick(tabHome);
        assertionsApp.assertVisible(tabHome);
        actionsApp.takeScreenshot();
    }
}
