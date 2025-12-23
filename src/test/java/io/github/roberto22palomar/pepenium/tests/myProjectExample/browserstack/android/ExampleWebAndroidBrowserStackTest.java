package io.github.roberto22palomar.pepenium.tests.myProjectExample.browserstack.android;

import io.github.roberto22palomar.pepenium.core.BaseTest;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.android.AndroidWebConfigBS;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.flows.ExampleNavigationFlow;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.HeaderPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.NavigationTabsPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import org.junit.jupiter.api.Test;

public class ExampleWebAndroidBrowserStackTest extends BaseTest {

    @Override
    protected DriverConfig getConfig() {
        return new AndroidWebConfigBS();
    }

    @Test
    public void basicWebNavigationFlow_shouldRunOnBrowserStackAndroidWeb() {
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
