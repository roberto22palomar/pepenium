package io.github.roberto22palomar.pepenium.smoke.android;

import io.appium.java_client.AppiumBy;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsApp;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsApp;
import org.openqa.selenium.By;

final class AndroidSearchPage {

    private static final By QUICK_SEARCH_TAB = AppiumBy.accessibilityId("btn-search-tab-search");

    private final ActionsApp actions;
    private final AssertionsApp assertions;

    AndroidSearchPage(ActionsApp actions, AssertionsApp assertions) {
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
