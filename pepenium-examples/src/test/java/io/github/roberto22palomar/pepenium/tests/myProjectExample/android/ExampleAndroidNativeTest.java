package io.github.roberto22palomar.pepenium.tests.myProjectExample.android;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.BaseTest;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsApp;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsApp;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.flows.ExampleNavigationFlowAndroid;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.pages.BottomNavigationPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.pages.SearchPage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

@Slf4j
@Tag("pepenium-example")
public class ExampleAndroidNativeTest extends BaseTest {

    private static final By SEARCH_QUICK_TAB = By.xpath("//*[@resource-id='btn-search-tab-search']");
    private static final By HOME_TAB = By.xpath("//*[@resource-id='click_bottom_home']");

    @Override
    protected TestTarget getTarget() {
        return TestTarget.ANDROID_NATIVE;
    }

    @Test
    void basicNavigationFlow_shouldRun() {
        ActionsApp actionsApp = new ActionsApp(appiumDriver());
        AssertionsApp assertionsApp = new AssertionsApp(appiumDriver());
        SearchPage searchPage = new SearchPage(actionsApp);
        BottomNavigationPage bottomNavigationPage = new BottomNavigationPage(actionsApp);
        ExampleNavigationFlowAndroid flow = new ExampleNavigationFlowAndroid(bottomNavigationPage, searchPage);
        assertionsApp.assertVisible(SEARCH_QUICK_TAB);
        flow.runBasicNavigationFlow();
        assertionsApp.assertVisible(HOME_TAB);
        log.info("Example Android native flow finished");
    }
}
