package io.github.roberto22palomar.pepenium.tests.myProjectExample.ios;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.BaseTest;
import io.appium.java_client.AppiumBy;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.flows.ExampleIOSShowcaseFlow;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.pages.BottomNavigationPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.pages.SearchPageIOS;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Slf4j
@Tag("pepenium-example")
public class ExampleIOSNativeTest extends BaseTest {

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
        ExampleIOSShowcaseFlow flow = new ExampleIOSShowcaseFlow(
                bottomNavigationPage,
                searchPage,
                assertionsApp,
                this::step
        );
        flow.runShowcaseFlow();
        log.info("Example iOS native flow finished");
    }
}
