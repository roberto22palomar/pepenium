package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.flows;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumSteps;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.pages.BottomNavigationPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.pages.SearchPage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExampleAndroidShowcaseFlow {

    @PepeniumInject
    private BottomNavigationPage bottomNavigationPage;

    @PepeniumInject
    private SearchPage searchPage;

    @PepeniumInject
    private PepeniumSteps stepRecorder;

    public void runShowcaseFlow() {
        log.info("=== Starting ExampleAndroidShowcaseFlow ===");

        stepRecorder.step("Validate Android search surface");
        searchPage.waitUntilLoaded();
        searchPage.assertSearchSurfaceVisible();

        stepRecorder.step("Open quick search tab");
        searchPage.openQuickSearchTab();
        searchPage.assertFiltersTabVisible();

        stepRecorder.step("Open filters tab");
        searchPage.openFiltersTab();
        searchPage.assertCloseButtonVisible();

        stepRecorder.step("Close search surface");
        searchPage.close();

        stepRecorder.step("Validate bottom navigation surface");
        bottomNavigationPage.waitUntilLoaded();
        bottomNavigationPage.assertNavigationVisible();

        stepRecorder.step("Navigate to tab one");
        bottomNavigationPage.openTabOne();
        bottomNavigationPage.assertHomeVisible();

        stepRecorder.step("Navigate to tab two");
        bottomNavigationPage.openTabTwo();
        bottomNavigationPage.assertTabThreeVisible();

        stepRecorder.step("Navigate to tab three");
        bottomNavigationPage.openTabThree();
        bottomNavigationPage.assertHomeVisible();

        stepRecorder.step("Return to home tab");
        bottomNavigationPage.openHome();
        bottomNavigationPage.assertHomeVisible();

        log.info("=== Finished ExampleAndroidShowcaseFlow ===");
    }
}
