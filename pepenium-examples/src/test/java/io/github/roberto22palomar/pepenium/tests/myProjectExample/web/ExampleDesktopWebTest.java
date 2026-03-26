package io.github.roberto22palomar.pepenium.tests.myProjectExample.web;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.BaseTest;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.flows.ExampleAuthenticationFlow;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages.AddRemoveElementsPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages.CheckboxesPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages.DropdownPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages.LoginPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages.SecureAreaPage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

@Slf4j
@Tag("pepenium-example")
public class ExampleDesktopWebTest extends BaseTest {

    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");

    @Override
    protected TestTarget getTarget() {
        return TestTarget.WEB_DESKTOP;
    }

    @Test
    void basicNavigationFlow_shouldRun() {
        ActionsWeb actionsWeb = new ActionsWeb(driver);
        AssertionsWeb assertionsWeb = new AssertionsWeb(driver);
        LoginPage loginPage = new LoginPage(actionsWeb);
        SecureAreaPage secureAreaPage = new SecureAreaPage(actionsWeb);
        DropdownPage dropdownPage = new DropdownPage(driver, actionsWeb);
        CheckboxesPage checkboxesPage = new CheckboxesPage(driver, actionsWeb);
        AddRemoveElementsPage addRemoveElementsPage = new AddRemoveElementsPage(driver, actionsWeb);
        ExampleAuthenticationFlow flow = new ExampleAuthenticationFlow(
                loginPage,
                secureAreaPage,
                dropdownPage,
                checkboxesPage,
                addRemoveElementsPage,
                assertionsWeb,
                this::step
        );
        String baseUrl = System.getenv().getOrDefault("PEPENIUM_BASE_URL", "https://the-internet.herokuapp.com/login");
        String username = System.getenv().getOrDefault("PEPENIUM_WEB_USERNAME", "tomsmith");
        String password = System.getenv().getOrDefault("PEPENIUM_WEB_PASSWORD", "SuperSecretPassword!");
        driver.get(baseUrl);
        assertionsWeb.assertVisible(LOGIN_BUTTON);
        flow.runSuccessfulLoginAndDropdownSelection(baseUrl, username, password);
        log.info("Example desktop web flow finished");
    }
}
