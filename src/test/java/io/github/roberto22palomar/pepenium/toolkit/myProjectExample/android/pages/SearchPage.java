package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages;

import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsApp;
import io.github.roberto22palomar.pepenium.toolkit.utils.AssertionsApp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
@RequiredArgsConstructor
public class SearchPage {

    // ============================================================
    // Generic locators (example) - Android
    // ============================================================
    private final By btnClose = By.xpath("//*[@resource-id='btn-search-close']");
    private final By tabQuickSearch = By.xpath("//*[@resource-id='btn-search-tab-search']");
    private final By tabFilters = By.xpath("//*[@resource-id='btn-search-tab-filter']");

    private final ActionsApp actionsApp;
    private final AssertionsApp assertionsApp;

    // ============================================================
    // Assertions and Actions
    // ============================================================

    public void assertSearchPageLoaded() {
        log.info("Asserting Search page is loaded (Android)");
        assertionsApp.assertVisible(btnClose);
        assertionsApp.assertVisible(tabQuickSearch);
        assertionsApp.assertVisible(tabFilters);
    }

    public void close() {
        log.info("Closing Search page (Android)");
        assertionsApp.assertClickable(btnClose);
        actionsApp.makeClick(btnClose);
        actionsApp.takeScreenshot();
    }

    public void openQuickSearchTab() {
        log.info("Opening Quick Search tab (Android)");
        assertionsApp.assertClickable(tabQuickSearch);
        actionsApp.makeClick(tabQuickSearch);
        assertionsApp.assertVisible(tabQuickSearch);
        actionsApp.takeScreenshot();
    }

    public void openFiltersTab() {
        log.info("Opening Filters tab (Android)");
        assertionsApp.assertClickable(tabFilters);
        actionsApp.makeClick(tabFilters);
        assertionsApp.assertVisible(tabFilters);
        actionsApp.takeScreenshot();
    }
}
