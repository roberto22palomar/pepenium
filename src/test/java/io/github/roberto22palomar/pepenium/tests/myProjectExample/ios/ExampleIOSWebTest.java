package io.github.roberto22palomar.pepenium.tests.myProjectExample.ios;

import io.github.roberto22palomar.pepenium.core.BaseTest;
import io.github.roberto22palomar.pepenium.core.TestTarget;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.flows.ExampleNavigationFlow;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.HeaderPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.NavigationTabsPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ExampleIOSWebTest extends BaseTest {

    @Override
    protected TestTarget getTarget() {
        return TestTarget.IOS_WEB;
    }

    @Override
    protected String getDefaultProfileId() {
        return "browserstack-ios-web";
    }

    @Test
    void basicNavigationFlow_shouldRun() {
        ActionsWeb actionsWeb = new ActionsWeb(driver);
        HeaderPage headerPage = new HeaderPage(actionsWeb);
        NavigationTabsPage navigationTabsPage = new NavigationTabsPage(actionsWeb);
        ExampleNavigationFlow flow = new ExampleNavigationFlow(headerPage, navigationTabsPage);
        String baseUrl = System.getenv().getOrDefault("PEPENIUM_BASE_URL", "https://example.com");
        driver.get(baseUrl);
        flow.runBasicNavigationFlow();
        log.info("Example iOS web flow finished");
    }
}
