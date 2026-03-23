package io.github.roberto22palomar.pepenium.tests.myProjectExample.browserstack.ios;

import io.github.roberto22palomar.pepenium.core.BaseTest;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.ios.IOSWebConfigBS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.flows.ExampleNavigationFlow;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.HeaderPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.NavigationTabsPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.utils.AssertionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfigMobile;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ExampleWebIOSBrowserStackTest extends BaseTest {

    @Override
    protected DriverConfig getConfig() {
        return new IOSWebConfigBS();
    }

    @Override
    protected boolean useAutomaticLifecycle() {
        return false;
    }

    static Stream<Arguments> platforms() {
        return IOSWebConfigBS.platforms();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("platforms")
    public void basicWebNavigationFlow_shouldRunOnBrowserStackIOS(BrowserStackConfigMobile.Platform platform) throws Exception {
        runWithConfig(new IOSWebConfigBS(platform), () -> {
            ActionsWeb actionsWeb = new ActionsWeb(driver);
            AssertionsWeb assertionsWeb = new AssertionsWeb(driver, actionsWeb);
            HeaderPage headerPage = new HeaderPage(actionsWeb, assertionsWeb);
            NavigationTabsPage navigationTabsPage = new NavigationTabsPage(actionsWeb, assertionsWeb);
            ExampleNavigationFlow flow = new ExampleNavigationFlow(headerPage, navigationTabsPage);
            String baseUrl = System.getenv().getOrDefault("PEPENIUM_BASE_URL", "https://example.com");
            driver.get(baseUrl);
            assertionsWeb.assertDocumentReady();
            assertionsWeb.assertCurrentUrlMatchesBase(baseUrl);
            flow.runBasicNavigationFlow();
        });
    }
}
