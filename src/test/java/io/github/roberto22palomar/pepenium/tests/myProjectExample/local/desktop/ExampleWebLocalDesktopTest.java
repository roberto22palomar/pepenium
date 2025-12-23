package io.github.roberto22palomar.pepenium.tests.myProjectExample.local.desktop;

import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.flows.ExampleNavigationFlow;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.HeaderPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.NavigationTabsPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@Slf4j
public class ExampleWebLocalDesktopTest {

    @Test
    void basicWebNavigationFlow_shouldRunLocally() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");

        WebDriver driver = new ChromeDriver(options);

        try {
            ActionsWeb actionsWeb = new ActionsWeb(driver);

            // Pages (example)
            HeaderPage headerPage = new HeaderPage(actionsWeb);
            NavigationTabsPage navigationTabsPage = new NavigationTabsPage(actionsWeb);

            // Flow (example)
            ExampleNavigationFlow flow = new ExampleNavigationFlow(headerPage, navigationTabsPage);

            // Target URL (example): env var with safe fallback
            String baseUrl = System.getenv().getOrDefault("PEPENIUM_BASE_URL", "https://example.com");
            driver.get(baseUrl);

            flow.runBasicNavigationFlow();

            log.info("Example web local flow finished");
        } finally {
            driver.quit();
        }
    }
}
