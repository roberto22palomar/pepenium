package io.github.roberto22palomar.pepenium.tests.myProjectExample.browserstack.android;

import io.github.roberto22palomar.pepenium.core.BaseTest;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.android.AndroidWebConfigBS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.flows.ExampleNavigationFlow;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.HeaderPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.NavigationTabsPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfigMobile;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ExampleWebAndroidBrowserStackTest extends BaseTest {

    @Override
    protected DriverConfig getConfig() {
        return new AndroidWebConfigBS();
    }

    @Override
    protected boolean useAutomaticLifecycle() {
        return false;
    }

    static Stream<Arguments> platforms() {
        return AndroidWebConfigBS.platforms();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("platforms")
    public void basicWebNavigationFlow_shouldRunOnBrowserStackAndroidWeb(BrowserStackConfigMobile.Platform platform) throws Exception {
        runWithConfig(new AndroidWebConfigBS(platform), () -> {
            ActionsWeb actionsWeb = new ActionsWeb(driver);
            HeaderPage headerPage = new HeaderPage(actionsWeb);
            NavigationTabsPage navigationTabsPage = new NavigationTabsPage(actionsWeb);
            ExampleNavigationFlow flow = new ExampleNavigationFlow(headerPage, navigationTabsPage);
            String baseUrl = System.getenv().getOrDefault("PEPENIUM_BASE_URL", "https://example.com");
            driver.get(baseUrl);
            flow.runBasicNavigationFlow();
        });
    }
}
