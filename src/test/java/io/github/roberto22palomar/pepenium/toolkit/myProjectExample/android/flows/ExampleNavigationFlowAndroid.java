package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.flows;


import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages.BottomNavigationPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages.SearchPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ExampleNavigationFlowAndroid {

    private final BottomNavigationPage bottomNavigationPage;
    private final SearchPage searchPage;

    /**
     * Example flow:
     * - Open Search
     * - Switch tabs inside Search (quick search / filters)
     * - Close Search
     * - Navigate through bottom tabs
     */
    public void runBasicNavigationFlow() {
        log.info("=== Starting ExampleNavigationFlowAndroid ===");

        // In a real app, opening the search could happen via a header button.
        // For this example, we assume the Search page can be reached and exercised directly.
        searchPage.openQuickSearchTab();
        searchPage.openFiltersTab();
        searchPage.close();

        bottomNavigationPage.openTabOne();
        bottomNavigationPage.openTabTwo();
        bottomNavigationPage.openHome();

        log.info("=== Finished ExampleNavigationFlowAndroid ===");
    }
}
