package io.github.roberto22palomar.pepenium.tests.myProjectExample.android;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.BaseTest;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.flows.ExampleNavigationFlow;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.HeaderPage;
import io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages.NavigationTabsPage;
import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ExampleAndroidWebTest extends BaseTest {

    @Override
    protected TestTarget getTarget() {
        return TestTarget.ANDROID_WEB;
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
        log.info("Example Android web flow finished");
    }
}
