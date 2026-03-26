package io.github.roberto22palomar.pepenium.tests.myProjectExample.web;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.BaseTest;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.flows.ExampleNavigationFlow;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages.HeaderPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages.NavigationTabsPage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

@Slf4j
@Tag("pepenium-example")
public class ExampleDesktopWebTest extends BaseTest {

    private static final By SEARCH_BUTTON = By.xpath("//*[@data-testid='HEADER-search']");
    private static final By FIRST_TAB = By.xpath("//*[@data-testid='TABS-ONE']");

    @Override
    protected TestTarget getTarget() {
        return TestTarget.WEB_DESKTOP;
    }

    @Test
    void basicNavigationFlow_shouldRun() {
        ActionsWeb actionsWeb = new ActionsWeb(driver);
        AssertionsWeb assertionsWeb = new AssertionsWeb(driver);
        HeaderPage headerPage = new HeaderPage(actionsWeb);
        NavigationTabsPage navigationTabsPage = new NavigationTabsPage(actionsWeb);
        ExampleNavigationFlow flow = new ExampleNavigationFlow(headerPage, navigationTabsPage);
        String baseUrl = System.getenv().getOrDefault("PEPENIUM_BASE_URL", "https://example.com");
        driver.get(baseUrl);
        assertionsWeb.assertVisible(SEARCH_BUTTON);
        assertionsWeb.assertVisible(FIRST_TAB);
        flow.runBasicNavigationFlow();
        assertionsWeb.assertVisible(SEARCH_BUTTON);
        log.info("Example desktop web flow finished");
    }
}
