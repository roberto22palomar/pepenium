package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages;

import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
@RequiredArgsConstructor
public class HeaderPage {

    // ============================================================
    // Generic locators (example) - XPath only
    // ============================================================
    private final By sideMenuButton = By.xpath("//*[@data-testid='HOME-menu']");
    private final By searchButton = By.xpath("//*[@data-testid='HEADER-search']");
    private final By refreshButton = By.xpath("//*[@data-testid='HEADER-refresh']");
    private final By moreOptionsButton = By.xpath("//*[@data-testid='HEADER-3points']");

    private final ActionsWeb actionsWeb;

    // ============================================================
    // Actions
    // ============================================================

    public void openSideMenu() {
        log.info("Opening side menu");
        actionsWeb.waitToBeVisible(sideMenuButton);
        actionsWeb.click(sideMenuButton);
        actionsWeb.takeScreenshot();
    }

    public void openSearch() {
        log.info("Opening search");
        actionsWeb.waitToBeVisible(searchButton);
        actionsWeb.click(searchButton);
        actionsWeb.takeScreenshot();
    }

    public void refresh() {
        log.info("Refreshing page");
        actionsWeb.waitToBeVisible(refreshButton);
        actionsWeb.click(refreshButton);
        actionsWeb.takeScreenshot();
    }

    public void openMoreOptions() {
        log.info("Opening header options menu");
        actionsWeb.waitToBeVisible(moreOptionsButton);
        actionsWeb.click(moreOptionsButton);
        actionsWeb.takeScreenshot();
    }
}
