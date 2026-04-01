package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.pages;

import io.appium.java_client.AppiumBy;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsAppIOS;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
public class SearchPageIOS {

    // ============================================================
    // Generic locators (example)
    // ============================================================
    private final By btnClose = AppiumBy.accessibilityId("btn-search-close");
    private final By tabQuickSearch = AppiumBy.accessibilityId("btn-search-tab-search");
    private final By tabFilters = AppiumBy.accessibilityId("btn-search-tab-filter");

    @PepeniumInject
    private ActionsAppIOS actionsApp;

    // ============================================================
    // Actions
    // ============================================================

    public void waitUntilLoaded() {
        actionsApp.waitToBePresent(tabQuickSearch);
        actionsApp.waitToBePresent(tabFilters);
        actionsApp.waitToBePresent(btnClose);
        actionsApp.takeScreenshotFast();
    }

    public By quickSearchTab() {
        return tabQuickSearch;
    }

    public By filtersTab() {
        return tabFilters;
    }

    public By closeButton() {
        return btnClose;
    }

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
