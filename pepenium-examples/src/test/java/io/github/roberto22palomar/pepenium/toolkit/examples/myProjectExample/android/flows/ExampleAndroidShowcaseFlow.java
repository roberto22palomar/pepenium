package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.flows;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumSteps;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsApp;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.pages.BottomNavigationPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.pages.SearchPage;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
public class ExampleAndroidShowcaseFlow {

    private static final By SEARCH_QUICK_TAB = By.xpath("//*[@resource-id='btn-search-tab-search']");
    private static final By SEARCH_FILTERS_TAB = By.xpath("//*[@resource-id='btn-search-tab-filter']");
    private static final By SEARCH_CLOSE_BUTTON = By.xpath("//*[@resource-id='btn-search-close']");
    private static final By TAB_ONE = By.xpath("//*[@resource-id='click-bottom-tab-1']");
    private static final By TAB_TWO = By.xpath("//*[@resource-id='click-bottom-tab-2']");
    private static final By TAB_THREE = By.xpath("//*[@resource-id='click-bottom-tab-3']");
    private static final By HOME_TAB = By.xpath("//*[@resource-id='click_bottom_home']");

    @PepeniumInject
    private BottomNavigationPage bottomNavigationPage;

    @PepeniumInject
    private SearchPage searchPage;

    @PepeniumInject
    private AssertionsApp assertionsApp;

    @PepeniumInject
    private PepeniumSteps stepRecorder;

    public void runShowcaseFlow() {
        log.info("=== Starting ExampleAndroidShowcaseFlow ===");

        stepRecorder.step("Validate Android search surface");
        searchPage.waitUntilLoaded();
        assertionsApp.assertVisible(SEARCH_QUICK_TAB);
        assertionsApp.assertVisible(SEARCH_FILTERS_TAB);
        assertionsApp.assertVisible(SEARCH_CLOSE_BUTTON);

        stepRecorder.step("Open quick search tab");
        searchPage.openQuickSearchTab();
        assertionsApp.assertVisible(SEARCH_FILTERS_TAB);

        stepRecorder.step("Open filters tab");
        searchPage.openFiltersTab();
        assertionsApp.assertVisible(SEARCH_CLOSE_BUTTON);

        stepRecorder.step("Close search surface");
        searchPage.close();

        stepRecorder.step("Validate bottom navigation surface");
        bottomNavigationPage.waitUntilLoaded();
        assertionsApp.assertVisible(TAB_ONE);
        assertionsApp.assertVisible(TAB_TWO);
        assertionsApp.assertVisible(TAB_THREE);
        assertionsApp.assertVisible(HOME_TAB);

        stepRecorder.step("Navigate to tab one");
        bottomNavigationPage.openTabOne();
        assertionsApp.assertVisible(HOME_TAB);

        stepRecorder.step("Navigate to tab two");
        bottomNavigationPage.openTabTwo();
        assertionsApp.assertVisible(TAB_THREE);

        stepRecorder.step("Navigate to tab three");
        bottomNavigationPage.openTabThree();
        assertionsApp.assertVisible(HOME_TAB);

        stepRecorder.step("Return to home tab");
        bottomNavigationPage.openHome();
        assertionsApp.assertVisible(HOME_TAB);

        log.info("=== Finished ExampleAndroidShowcaseFlow ===");
    }
}
