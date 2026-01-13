package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages;

import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsAppIOS;
import io.appium.java_client.AppiumBy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
@RequiredArgsConstructor
public class SearchPageIOS {

    // ============================================================
    // Generic locators (example)
    // ============================================================
    private final By btnClose = AppiumBy.accessibilityId("btn-search-close");
    private final By tabQuickSearch = AppiumBy.accessibilityId("btn-search-tab-search");
    private final By tabFilters = AppiumBy.accessibilityId("btn-search-tab-filter");

    private final ActionsAppIOS actionsApp;

    // ============================================================
    // Actions
    // ============================================================

    public void close() {
        log.info("Closing Search page (iOS)");
        actionsApp.click(btnClose);
        actionsApp.takeScreenshot();
    }

    public void openQuickSearchTab() {
        log.info("Opening Quick Search tab (iOS)");
        actionsApp.click(tabQuickSearch);
        actionsApp.takeScreenshot();
    }

    public void openFiltersTab() {
        log.info("Opening Filters tab (iOS)");
        actionsApp.click(tabFilters);
        actionsApp.takeScreenshot();
    }
}
