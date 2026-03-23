package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages;

import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.utils.AssertionsAppIOS;
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
    private final AssertionsAppIOS assertionsAppIOS;

    // ============================================================
    // Assertions and Actions
    // ============================================================

    public void assertSearchPageLoaded() {
        log.info("Asserting Search page is loaded (iOS)");
        assertionsAppIOS.assertVisible(btnClose);
        assertionsAppIOS.assertVisible(tabQuickSearch);
        assertionsAppIOS.assertVisible(tabFilters);
    }

    public void close() {
        log.info("Closing Search page (iOS)");
        assertionsAppIOS.assertClickable(btnClose);
        actionsApp.click(btnClose);
        actionsApp.takeScreenshot();
    }

    public void openQuickSearchTab() {
        log.info("Opening Quick Search tab (iOS)");
        assertionsAppIOS.assertClickable(tabQuickSearch);
        actionsApp.click(tabQuickSearch);
        assertionsAppIOS.assertVisible(tabQuickSearch);
        actionsApp.takeScreenshot();
    }

    public void openFiltersTab() {
        log.info("Opening Filters tab (iOS)");
        assertionsAppIOS.assertClickable(tabFilters);
        actionsApp.click(tabFilters);
        assertionsAppIOS.assertVisible(tabFilters);
        actionsApp.takeScreenshot();
    }
}
