package io.github.roberto22palomar.pepenium.tests.myProjectExample.browserstack.desktop;

import io.github.roberto22palomar.pepenium.core.configs.browserstack.desktop.MacWebConfigBS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.flows.ExampleNavigationFlow;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.HeaderPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.NavigationTabsPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.utils.AssertionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfigDesktop;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebDriver;

import java.util.stream.Stream;

public class ExampleWebMacBrowserStackTest {

    static Stream<Arguments> platforms() {
        return MacWebConfigBS.platforms();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("platforms")
    public void basicWebNavigationFlow_shouldRunOnBrowserStackMacDesktop(BrowserStackConfigDesktop.Platform platform) throws Exception {
        WebDriver driver = new MacWebConfigBS(platform).createDriver();
        try {
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
        } finally {
            driver.quit();
        }
    }
}
