package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.pages;

import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsApp;
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

    public void waitUntilLoaded() {
        actionsApp.waitToBePresent(tabQuickSearch);
        actionsApp.waitToBePresent(tabFilters);
        actionsApp.waitToBePresent(btnClose);
    }

    public void close() {
        log.info("Closing Search page (Android)");
        actionsApp.click(btnClose);
        actionsApp.takeScreenshot();
    }

    public void openQuickSearchTab() {
        log.info("Opening Quick Search tab (Android)");
        actionsApp.click(tabQuickSearch);
        actionsApp.takeScreenshot();
    }

    public void openFiltersTab() {
        log.info("Opening Filters tab (Android)");
        actionsApp.click(tabFilters);
        actionsApp.takeScreenshot();
    }
}
