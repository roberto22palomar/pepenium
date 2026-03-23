package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages;

import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.utils.AssertionsWeb;
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
    private final AssertionsWeb assertionsWeb;

    // ============================================================
    // Assertions and Actions
    // ============================================================

    public void assertHeaderLoaded() {
        log.info("Asserting header is loaded");
        assertionsWeb.assertVisible(sideMenuButton);
        assertionsWeb.assertVisible(searchButton);
        assertionsWeb.assertVisible(refreshButton);
        assertionsWeb.assertVisible(moreOptionsButton);
    }

    public void openSideMenu() {
        log.info("Opening side menu");
        assertionsWeb.assertClickable(sideMenuButton);
        actionsWeb.click(sideMenuButton);
        actionsWeb.takeScreenshot();
    }

    public void openSearch() {
        log.info("Opening search");
        assertionsWeb.assertClickable(searchButton);
        actionsWeb.click(searchButton);
        actionsWeb.takeScreenshot();
    }

    public void refresh() {
        log.info("Refreshing page");
        assertionsWeb.assertClickable(refreshButton);
        actionsWeb.click(refreshButton);
        actionsWeb.takeScreenshot();
    }

    public void openMoreOptions() {
        log.info("Opening header options menu");
        assertionsWeb.assertClickable(moreOptionsButton);
        actionsWeb.click(moreOptionsButton);
        actionsWeb.takeScreenshot();
    }
}
