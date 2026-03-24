package io.github.roberto22palomar.pepenium.tests.myProjectExample.android;

import io.github.roberto22palomar.pepenium.core.BaseTest;
import io.github.roberto22palomar.pepenium.core.TestTarget;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.flows.ExampleNavigationFlowAndroid;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages.BottomNavigationPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages.SearchPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsApp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ExampleAndroidNativeTest extends BaseTest {

    @Override
    protected TestTarget getTarget() {
        return TestTarget.ANDROID_NATIVE;
    }

    @Test
    void basicNavigationFlow_shouldRun() {
        ActionsApp actionsApp = new ActionsApp(appiumDriver());
        SearchPage searchPage = new SearchPage(actionsApp);
        BottomNavigationPage bottomNavigationPage = new BottomNavigationPage(actionsApp);
        ExampleNavigationFlowAndroid flow = new ExampleNavigationFlowAndroid(bottomNavigationPage, searchPage);
        flow.runBasicNavigationFlow();
        log.info("Example Android native flow finished");
    }
}
