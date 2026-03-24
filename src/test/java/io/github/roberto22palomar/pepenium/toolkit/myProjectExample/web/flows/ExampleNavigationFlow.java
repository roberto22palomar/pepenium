package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.flows;


import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.HeaderPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.NavigationTabsPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ExampleNavigationFlow {

    private final HeaderPage headerPage;
    private final NavigationTabsPage navigationTabsPage;

    /**
     * Example flow:
     * - Open search from header
     * - Switch through a couple of navigation tabs
     * - Open overflow menu and click a generic option
     * - Refresh the page
     */
    public void runBasicNavigationFlow() {
        log.info("=== Starting ExampleNavigationFlow ===");

        headerPage.openSearch();

        navigationTabsPage.open(NavigationTabsPage.Tab.ONE);
        navigationTabsPage.open(NavigationTabsPage.Tab.THREE);

        headerPage.openMoreOptions();

        headerPage.refresh();

        log.info("=== Finished ExampleNavigationFlow ===");
    }

    /**
     * Example flow:
     * - Open side menu
     * - Go back home (tab)
     * - Open overflow menu and print
     */
    public void runMenuAndHomeFlow() {
        log.info("=== Starting MenuAndHomeFlow ===");

        headerPage.openSideMenu();

        // If your app has a "home" tab mapped as ONE, keep it like this for the example.
        // Otherwise change the enum mapping as needed.
        navigationTabsPage.open(NavigationTabsPage.Tab.ONE);

        headerPage.openMoreOptions();

        log.info("=== Finished MenuAndHomeFlow ===");
    }
}
