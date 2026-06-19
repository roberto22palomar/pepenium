package io.github.roberto22palomar.pepenium.smoke.android;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsApp;
import io.github.roberto22palomar.pepenium.toolkit.locators.PepeniumBy;
import org.openqa.selenium.By;

final class AndroidSearchPage {

    private static final By QUICK_SEARCH_TAB = PepeniumBy.accessibilityId("btn-search-tab-search");

    @PepeniumInject
    private ActionsApp actions;

    void waitUntilLoaded() {
    }

    void openQuickSearch() {
        actions.click(QUICK_SEARCH_TAB);
    }

    By quickSearchTab() {
        return QUICK_SEARCH_TAB;
    }
}
