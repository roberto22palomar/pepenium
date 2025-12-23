package io.github.roberto22palomar.pepenium.tests.myProjectExample.browserstack.ios;

import io.github.roberto22palomar.pepenium.core.BaseTest;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.ios.IOSConfigBS;

import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.flows.ExampleNavigationFlowIOS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages.BottomNavigationPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages.SearchPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsAppIOS;
import org.junit.jupiter.api.Test;

public class ExampleIOSBrowserStackTest extends BaseTest {

    @Override
    protected DriverConfig getConfig() {
        return new IOSConfigBS();
    }

    @Test
    public void basicNavigationFlow_shouldRunOnBrowserStackIOS() {
        ActionsAppIOS actionsApp = new ActionsAppIOS(driver);

        // Pages (example)
        SearchPageIOS searchPage = new SearchPageIOS(actionsApp);
        BottomNavigationPageIOS bottomNavigationPage = new BottomNavigationPageIOS(actionsApp);

        // Flow (example)
        ExampleNavigationFlowIOS flow = new ExampleNavigationFlowIOS(bottomNavigationPage, searchPage);

        // Execute
        flow.runBasicNavigationFlow();
    }
}
