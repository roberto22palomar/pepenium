package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages;

import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsApp;
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

    // ============================================================
    // Actions
    // ============================================================

    public void close() {
        log.info("Closing Search page (Android)");
        actionsApp.makeClick(btnClose);
        actionsApp.takeScreenshot();
    }

    public void openQuickSearchTab() {
        log.info("Opening Quick Search tab (Android)");
        actionsApp.makeClick(tabQuickSearch);
        actionsApp.takeScreenshot();
    }

    public void openFiltersTab() {
        log.info("Opening Filters tab (Android)");
        actionsApp.makeClick(tabFilters);
        actionsApp.takeScreenshot();
    }
}
