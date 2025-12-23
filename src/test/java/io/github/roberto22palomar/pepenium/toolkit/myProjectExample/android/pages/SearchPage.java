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
        actionsApp.hacerClick(btnClose);
        actionsApp.hacerCapturaPantalla();
    }

    public void openQuickSearchTab() {
        log.info("Opening Quick Search tab (Android)");
        actionsApp.hacerClick(tabQuickSearch);
        actionsApp.hacerCapturaPantalla();
    }

    public void openFiltersTab() {
        log.info("Opening Filters tab (Android)");
        actionsApp.hacerClick(tabFilters);
        actionsApp.hacerCapturaPantalla();
    }
}
