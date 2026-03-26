package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.flows;

import io.appium.java_client.AppiumBy;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.pages.BottomNavigationPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.pages.SearchPageIOS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class ExampleIOSShowcaseFlow {

    private static final By SEARCH_QUICK_TAB = AppiumBy.accessibilityId("btn-search-tab-search");
    private static final By SEARCH_FILTERS_TAB = AppiumBy.accessibilityId("btn-search-tab-filter");
    private static final By SEARCH_CLOSE_BUTTON = AppiumBy.accessibilityId("btn-search-close");
    private static final By TAB_ONE = AppiumBy.accessibilityId("click-bottom-tab-1");
    private static final By TAB_TWO = AppiumBy.accessibilityId("click-bottom-tab-2");
    private static final By TAB_THREE = AppiumBy.accessibilityId("click-bottom-tab-3");
    private static final By HOME_TAB = AppiumBy.accessibilityId("click_bottom_home");

    private final BottomNavigationPageIOS bottomNavigationPage;
    private final SearchPageIOS searchPage;
    private final AssertionsAppIOS assertionsApp;
    private final Consumer<String> stepRecorder;

    public void runShowcaseFlow() {
        log.info("=== Starting ExampleIOSShowcaseFlow ===");

        stepRecorder.accept("Validate iOS search surface");
        searchPage.waitUntilLoaded();
        assertionsApp.assertVisible(SEARCH_QUICK_TAB);
        assertionsApp.assertVisible(SEARCH_FILTERS_TAB);
        assertionsApp.assertVisible(SEARCH_CLOSE_BUTTON);

        stepRecorder.accept("Open quick search tab");
        searchPage.openQuickSearchTab();
        assertionsApp.assertVisible(SEARCH_FILTERS_TAB);

        stepRecorder.accept("Open filters tab");
        searchPage.openFiltersTab();
        assertionsApp.assertVisible(SEARCH_CLOSE_BUTTON);

        stepRecorder.accept("Close search surface");
        searchPage.close();

        stepRecorder.accept("Validate bottom navigation surface");
        bottomNavigationPage.waitUntilLoaded();
        assertionsApp.assertVisible(TAB_ONE);
        assertionsApp.assertVisible(TAB_TWO);
        assertionsApp.assertVisible(TAB_THREE);
        assertionsApp.assertVisible(HOME_TAB);

        stepRecorder.accept("Navigate to tab one");
        bottomNavigationPage.openTabOne();
        assertionsApp.assertVisible(HOME_TAB);

        stepRecorder.accept("Navigate to tab two");
        bottomNavigationPage.openTabTwo();
        assertionsApp.assertVisible(TAB_THREE);

        stepRecorder.accept("Navigate to tab three");
        bottomNavigationPage.openTabThree();
        assertionsApp.assertVisible(HOME_TAB);

        stepRecorder.accept("Return to home tab");
        bottomNavigationPage.openHome();
        assertionsApp.assertVisible(HOME_TAB);

        log.info("=== Finished ExampleIOSShowcaseFlow ===");
    }
}
