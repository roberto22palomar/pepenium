package io.github.roberto22palomar.pepenium.tests.myProjectExample.browserstack.android;

import io.github.roberto22palomar.pepenium.core.BaseTest;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.android.AndroidConfigBS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.flows.ExampleNavigationFlowAndroid;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages.BottomNavigationPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.android.pages.SearchPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsApp;
import io.github.roberto22palomar.pepenium.toolkit.utils.AssertionsApp;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ExampleAndroidBSTest extends BaseTest {

    @Override
    protected DriverConfig getConfig() {
        return new AndroidConfigBS();
    }

    @Override
    protected boolean useAutomaticLifecycle() {
        return false;
    }

    static Stream<Arguments> platforms() {
        return AndroidConfigBS.platforms();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("platforms")
    public void basicNavigationFlow_shouldRunOnBrowserStackAndroid(BrowserStackConfig.Platform platform) throws Exception {
        runWithConfig(new AndroidConfigBS(platform), () -> {
            ActionsApp actionsApp = new ActionsApp(driver);
            AssertionsApp assertionsApp = new AssertionsApp(driver, actionsApp);
            SearchPage searchPage = new SearchPage(actionsApp, assertionsApp);
            BottomNavigationPage bottomNavigationPage = new BottomNavigationPage(actionsApp, assertionsApp);
            ExampleNavigationFlowAndroid flow = new ExampleNavigationFlowAndroid(bottomNavigationPage, searchPage);
            flow.runBasicNavigationFlow();
        });
    }
}
