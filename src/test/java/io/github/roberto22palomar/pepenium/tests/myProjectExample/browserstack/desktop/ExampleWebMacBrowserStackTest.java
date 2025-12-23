package io.github.roberto22palomar.pepenium.tests.myProjectExample.browserstack.desktop;

import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.desktop.MacWebConfigBS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.flows.ExampleNavigationFlow;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.HeaderPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.NavigationTabsPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

public class ExampleWebMacBrowserStackTest {

    private WebDriver driver;

    @BeforeEach
    public void setup() throws Exception {
        driver = new MacWebConfigBS().createDriver();
    }

    @Test
    public void basicWebNavigationFlow_shouldRunOnBrowserStackMacDesktop() {
        ActionsWeb actionsWeb = new ActionsWeb(driver);

        // Pages (example)
        HeaderPage headerPage = new HeaderPage(actionsWeb);
        NavigationTabsPage navigationTabsPage = new NavigationTabsPage(actionsWeb);

        // Flow (example)
        ExampleNavigationFlow flow = new ExampleNavigationFlow(headerPage, navigationTabsPage);

        // Target URL (example)
        String baseUrl = System.getenv().getOrDefault("PEPENIUM_BASE_URL", "https://example.com");
        driver.get(baseUrl);

        // Execute
        flow.runBasicNavigationFlow();
    }
}
