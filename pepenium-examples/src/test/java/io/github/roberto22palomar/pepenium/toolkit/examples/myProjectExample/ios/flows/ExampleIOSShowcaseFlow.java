package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.flows;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumSteps;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.pages.BottomNavigationPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.pages.SearchPageIOS;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExampleIOSShowcaseFlow {

    @PepeniumInject
    private BottomNavigationPageIOS bottomNavigationPage;

    @PepeniumInject
    private SearchPageIOS searchPage;

    @PepeniumInject
    private AssertionsAppIOS assertionsApp;

    @PepeniumInject
    private PepeniumSteps stepRecorder;

    public void runShowcaseFlow() {
        log.info("=== Starting ExampleIOSShowcaseFlow ===");

        stepRecorder.step("Validate iOS search surface");
        searchPage.waitUntilLoaded();
        assertionsApp.assertVisible(searchPage.quickSearchTab());
        assertionsApp.assertVisible(searchPage.filtersTab());
        assertionsApp.assertVisible(searchPage.closeButton());

        stepRecorder.step("Open quick search tab");
        searchPage.openQuickSearchTab();
        assertionsApp.assertVisible(searchPage.filtersTab());

        stepRecorder.step("Open filters tab");
        searchPage.openFiltersTab();
        assertionsApp.assertVisible(searchPage.closeButton());

        stepRecorder.step("Close search surface");
        searchPage.close();

        stepRecorder.step("Validate bottom navigation surface");
        bottomNavigationPage.waitUntilLoaded();
        assertionsApp.assertVisible(bottomNavigationPage.tabOne());
        assertionsApp.assertVisible(bottomNavigationPage.tabTwo());
        assertionsApp.assertVisible(bottomNavigationPage.tabThree());
        assertionsApp.assertVisible(bottomNavigationPage.homeTab());

        stepRecorder.step("Navigate to tab one");
        bottomNavigationPage.openTabOne();
        assertionsApp.assertVisible(bottomNavigationPage.homeTab());

        stepRecorder.step("Navigate to tab two");
        bottomNavigationPage.openTabTwo();
        assertionsApp.assertVisible(bottomNavigationPage.tabThree());

        stepRecorder.step("Navigate to tab three");
        bottomNavigationPage.openTabThree();
        assertionsApp.assertVisible(bottomNavigationPage.homeTab());

        stepRecorder.step("Return to home tab");
        bottomNavigationPage.openHome();
        assertionsApp.assertVisible(bottomNavigationPage.homeTab());

        log.info("=== Finished ExampleIOSShowcaseFlow ===");
    }
}
