package io.github.roberto22palomar.pepenium.tests.myProjectExample.aws.ios;

import io.github.roberto22palomar.pepenium.core.BaseTest;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.configs.aws.ios.IOSConfigAWS;

import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.flows.ExampleNavigationFlowIOS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages.BottomNavigationPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages.SearchPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsAppIOS;
import org.junit.jupiter.api.Test;

public class ExampleIOSAWSTest extends BaseTest {

    @Override
    protected DriverConfig getConfig() {
        return new IOSConfigAWS();
    }

    @Test
    public void basicNavigationFlow_shouldRunOnAwsDeviceFarm() {
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
