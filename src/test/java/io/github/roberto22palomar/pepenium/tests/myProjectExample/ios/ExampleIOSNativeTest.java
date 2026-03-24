package io.github.roberto22palomar.pepenium.tests.myProjectExample.ios;

import io.github.roberto22palomar.pepenium.core.BaseTest;
import io.github.roberto22palomar.pepenium.core.TestTarget;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.flows.ExampleNavigationFlowIOS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages.BottomNavigationPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages.SearchPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsAppIOS;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
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
        SearchPageIOS searchPage = new SearchPageIOS(actionsApp);
        BottomNavigationPageIOS bottomNavigationPage = new BottomNavigationPageIOS(actionsApp);
        ExampleNavigationFlowIOS flow = new ExampleNavigationFlowIOS(bottomNavigationPage, searchPage);
        flow.runBasicNavigationFlow();
        log.info("Example iOS native flow finished");
    }
}
