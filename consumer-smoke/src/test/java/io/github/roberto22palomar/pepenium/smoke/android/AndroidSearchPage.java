package io.github.roberto22palomar.pepenium.smoke.android;

import io.appium.java_client.AppiumBy;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsApp;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsApp;
import org.openqa.selenium.By;

final class AndroidSearchPage {

    private static final By QUICK_SEARCH_TAB = AppiumBy.accessibilityId("btn-search-tab-search");

    @PepeniumInject
    private ActionsApp actions;

    @PepeniumInject
    private AssertionsApp assertions;

    void waitUntilLoaded() {
        assertions.assertVisible(QUICK_SEARCH_TAB);
    }

    void openQuickSearch() {
        actions.click(QUICK_SEARCH_TAB);
    }
}
