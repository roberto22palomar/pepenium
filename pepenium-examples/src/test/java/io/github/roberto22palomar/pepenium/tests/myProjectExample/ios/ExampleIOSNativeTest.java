package io.github.roberto22palomar.pepenium.tests.myProjectExample.ios;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.BaseTest;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.flows.ExampleNavigationFlowIOS;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.pages.BottomNavigationPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.pages.SearchPageIOS;
import io.appium.java_client.AppiumBy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

@Slf4j
@Tag("pepenium-example")
public class ExampleIOSNativeTest extends BaseTest {

    private static final By SEARCH_QUICK_TAB = AppiumBy.accessibilityId("btn-search-tab-search");
    private static final By HOME_TAB = AppiumBy.accessibilityId("click_bottom_home");

    @Override
    protected TestTarget getTarget() {
        return TestTarget.IOS_NATIVE;
    }

    @Override
    protected String getDefaultProfileId() {
        return "browserstack-ios";
    }

    @Test
    void basicNavigationFlow_shouldRun() {
        ActionsAppIOS actionsApp = new ActionsAppIOS(appiumDriver());
        AssertionsAppIOS assertionsApp = new AssertionsAppIOS(appiumDriver());
        SearchPageIOS searchPage = new SearchPageIOS(actionsApp);
        BottomNavigationPageIOS bottomNavigationPage = new BottomNavigationPageIOS(actionsApp);
        ExampleNavigationFlowIOS flow = new ExampleNavigationFlowIOS(bottomNavigationPage, searchPage);
        assertionsApp.assertVisible(SEARCH_QUICK_TAB);
        flow.runBasicNavigationFlow();
        assertionsApp.assertVisible(HOME_TAB);
        log.info("Example iOS native flow finished");
    }
}
