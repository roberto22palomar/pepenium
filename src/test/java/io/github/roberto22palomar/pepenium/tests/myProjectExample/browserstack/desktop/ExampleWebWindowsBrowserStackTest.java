package io.github.roberto22palomar.pepenium.tests.myProjectExample.browserstack.desktop;

import io.github.roberto22palomar.pepenium.core.configs.browserstack.desktop.WindowsWebConfigBS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.flows.ExampleNavigationFlow;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.HeaderPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.NavigationTabsPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.utils.BrowserStackConfigDesktop;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebDriver;

import java.util.stream.Stream;

public class ExampleWebWindowsBrowserStackTest {

    static Stream<Arguments> platforms() {
        return WindowsWebConfigBS.platforms();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("platforms")
    public void basicWebNavigationFlow_shouldRunOnBrowserStackWindowsDesktop(BrowserStackConfigDesktop.Platform platform) throws Exception {
        WebDriver driver = new WindowsWebConfigBS(platform).createDriver();
        try {
            ActionsWeb actionsWeb = new ActionsWeb(driver);
            HeaderPage headerPage = new HeaderPage(actionsWeb);
            NavigationTabsPage navigationTabsPage = new NavigationTabsPage(actionsWeb);
            ExampleNavigationFlow flow = new ExampleNavigationFlow(headerPage, navigationTabsPage);
            String baseUrl = System.getenv().getOrDefault("PEPENIUM_BASE_URL", "https://example.com");
            driver.manage().window().maximize();
            driver.get(baseUrl);
            flow.runBasicNavigationFlow();
        } finally {
            driver.quit();
        }
    }
}
