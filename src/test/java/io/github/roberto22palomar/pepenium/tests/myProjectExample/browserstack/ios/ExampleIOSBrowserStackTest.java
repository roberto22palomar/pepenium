package io.github.roberto22palomar.pepenium.tests.myProjectExample.browserstack.ios;

import io.github.roberto22palomar.pepenium.core.BaseTest;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.ios.IOSConfigBS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.flows.ExampleNavigationFlowIOS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages.BottomNavigationPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.ios.pages.SearchPageIOS;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.utils.AssertionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ExampleIOSBrowserStackTest extends BaseTest {

    @Override
    protected DriverConfig getConfig() {
        return new IOSConfigBS();
    }

    @Override
    protected boolean useAutomaticLifecycle() {
        return false;
    }

    static Stream<Arguments> platforms() {
        return IOSConfigBS.platforms();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("platforms")
    public void basicNavigationFlow_shouldRunOnBrowserStackIOS(BrowserStackConfig.Platform platform) throws Exception {
        runWithConfig(new IOSConfigBS(platform), () -> {
            ActionsAppIOS actionsApp = new ActionsAppIOS(driver);
            AssertionsAppIOS assertionsAppIOS = new AssertionsAppIOS(driver, actionsApp);
            assertionsAppIOS.assertStableScreen();
            SearchPageIOS searchPage = new SearchPageIOS(actionsApp, assertionsAppIOS);
            BottomNavigationPageIOS bottomNavigationPage = new BottomNavigationPageIOS(actionsApp, assertionsAppIOS);
            ExampleNavigationFlowIOS flow = new ExampleNavigationFlowIOS(bottomNavigationPage, searchPage);
            flow.runBasicNavigationFlow();
        });
    }
}
