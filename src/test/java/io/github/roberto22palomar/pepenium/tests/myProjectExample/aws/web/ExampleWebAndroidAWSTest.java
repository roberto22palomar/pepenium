package io.github.roberto22palomar.pepenium.tests.myProjectExample.aws.web;

import io.github.roberto22palomar.pepenium.core.BaseTest;
import io.github.roberto22palomar.pepenium.core.DriverConfig;
import io.github.roberto22palomar.pepenium.core.configs.aws.android.AndroidWebConfigAWS;

import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.flows.ExampleNavigationFlow;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.HeaderPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.NavigationTabsPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.utils.AssertionsWeb;
import org.junit.jupiter.api.Test;

public class ExampleWebAndroidAWSTest extends BaseTest {

    @Override
    protected DriverConfig getConfig() {
        return new AndroidWebConfigAWS();
    }

    @Test
    public void basicWebNavigationFlow_shouldRunOnAwsDeviceFarm() {
        ActionsWeb actionsWeb = new ActionsWeb(driver);
        AssertionsWeb assertionsWeb = new AssertionsWeb(driver, actionsWeb);

        // Pages (example)
        HeaderPage headerPage = new HeaderPage(actionsWeb, assertionsWeb);
        NavigationTabsPage navigationTabsPage = new NavigationTabsPage(actionsWeb, assertionsWeb);


        // Flow (example)
        ExampleNavigationFlow flow = new ExampleNavigationFlow(headerPage, navigationTabsPage);

        // Target URL (example): use env var, fallback to a safe default
        String baseUrl = System.getenv().getOrDefault("PEPENIUM_BASE_URL", "https://example.com");

        driver.get(baseUrl);
        assertionsWeb.assertDocumentReady();
        assertionsWeb.assertCurrentUrlMatchesBase(baseUrl);

        // Execute
        flow.runBasicNavigationFlow();
    }
}
