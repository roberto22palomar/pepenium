package io.github.roberto22palomar.pepenium.tests.myProjectExample.browserstack.android;

import io.github.roberto22palomar.pepenium.core.BaseTest;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.android.AndroidConfigBS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.flows.ExampleNavigationFlowAndroid;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages.BottomNavigationPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages.SearchPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsApp;
import org.junit.jupiter.api.Test;

public class ExampleAndroidBSTest extends BaseTest {

    @Override
    protected DriverConfig getConfig() {
        return new AndroidConfigBS();
    }

    @Test
    public void basicNavigationFlow_shouldRunOnAwsDeviceFarm() {
        ActionsApp actionsApp = new ActionsApp(driver);

        // Pages (example)
        SearchPage searchPage = new SearchPage(actionsApp);
        BottomNavigationPage bottomNavigationPage = new BottomNavigationPage(actionsApp);

        // Flow (example)
        ExampleNavigationFlowAndroid flow = new ExampleNavigationFlowAndroid(bottomNavigationPage, searchPage);

        // Execute
        flow.runBasicNavigationFlow();
    }
}

