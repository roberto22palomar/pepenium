package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.plugandplay;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumSteps;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlugAndPlayAuthenticationFlow {

    @PepeniumInject
    private PlugAndPlayLoginPage loginPage;

    @PepeniumInject
    private PlugAndPlaySecureAreaPage secureAreaPage;

    @PepeniumInject
    private PepeniumSteps steps;

    public void runSuccessfulLogin(String username, String password) {
        log.info("=== Starting PlugAndPlayAuthenticationFlow ===");

        steps.step("Validate login page");
        loginPage.assertLoaded();

        steps.step("Login with valid credentials");
        loginPage.login(username, password);

        steps.step("Verify secure area after successful login");
        secureAreaPage.assertSuccessfulLogin();

        log.info("=== Finished PlugAndPlayAuthenticationFlow ===");
    }
}
