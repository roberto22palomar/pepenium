package io.github.roberto22palomar.pepenium.smoke.ios;

import io.appium.java_client.AppiumBy;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsAppIOS;
import org.openqa.selenium.By;

final class IOSSearchPage {

    private static final By QUICK_SEARCH_TAB = AppiumBy.accessibilityId("btn-search-tab-search");

    private final ActionsAppIOS actions;
    private final AssertionsAppIOS assertions;

    IOSSearchPage(ActionsAppIOS actions, AssertionsAppIOS assertions) {
        this.actions = actions;
        this.assertions = assertions;
    }

    void waitUntilLoaded() {
        assertions.assertVisible(QUICK_SEARCH_TAB);
    }

    void openQuickSearch() {
        actions.click(QUICK_SEARCH_TAB);
    }
}
