package io.github.roberto22palomar.pepenium.smoke.ios;

import io.appium.java_client.AppiumBy;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsAppIOS;
import org.openqa.selenium.By;

final class IOSSearchPage {

    private static final By QUICK_SEARCH_TAB = AppiumBy.accessibilityId("btn-search-tab-search");

    @PepeniumInject
    private ActionsAppIOS actions;

    void waitUntilLoaded() {
    }

    void openQuickSearch() {
        actions.click(QUICK_SEARCH_TAB);
    }

    By quickSearchTab() {
        return QUICK_SEARCH_TAB;
    }
}
