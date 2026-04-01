package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.plugandplay;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumSteps;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlugAndPlayAuthenticationFlow {

    @PepeniumInject
    private PlugAndPlayLoginPage loginPage;

    @PepeniumInject
    private PlugAndPlaySecureAreaPage secureAreaPage;

    @PepeniumInject
    private AssertionsWeb assertionsWeb;

    @PepeniumInject
    private PepeniumSteps steps;

    public void runSuccessfulLogin(String username, String password) {
        log.info("=== Starting PlugAndPlayAuthenticationFlow ===");

        steps.step("Validate login page");
        assertionsWeb.assertVisible(loginPage.pageTitle());
        assertionsWeb.assertVisible(loginPage.usernameInput());
        assertionsWeb.assertVisible(loginPage.passwordInput());
        assertionsWeb.assertVisible(loginPage.loginButton());
        assertionsWeb.assertTextEquals(loginPage.pageTitle(), "Login Page");

        steps.step("Login with valid credentials");
        loginPage.login(username, password);

        steps.step("Verify secure area after successful login");
        assertionsWeb.assertVisible(secureAreaPage.logoutButton());
        assertionsWeb.assertTextContains(secureAreaPage.flashMessage(), "You logged into a secure area!");
        assertionsWeb.assertUrlContains("/secure");

        log.info("=== Finished PlugAndPlayAuthenticationFlow ===");
    }
}
