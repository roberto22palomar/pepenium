package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.flows;


import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages.BottomNavigationPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages.SearchPageIOS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ExampleNavigationFlowIOS {

    private final BottomNavigationPageIOS bottomNavigationPage;
    private final SearchPageIOS searchPage;

    /**
     * Example flow:
     * - Use Search
     * - Navigate through bottom tabs
     * - Return to Home
     */
    public void runBasicNavigationFlow() {
        log.info("=== Starting ExampleNavigationFlowIOS ===");

        searchPage.openQuickSearchTab();
        searchPage.openFiltersTab();
        searchPage.close();

        bottomNavigationPage.openTabOne();
        bottomNavigationPage.openTabThree();
        bottomNavigationPage.openHome();

        log.info("=== Finished ExampleNavigationFlowIOS ===");
    }
}
