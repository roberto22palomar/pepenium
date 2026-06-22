package io.github.roberto22palomar.pepenium.smoke.web;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumSteps;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb;

final class WebLoginFlow {

    @PepeniumInject
    private WebLoginPage loginPage;

    @PepeniumInject
    private AssertionsWeb assertions;

    @PepeniumInject
    private PepeniumSteps stepRecorder;

    void loginWithValidCredentials() {
        stepRecorder.step("Open login page");
        assertions.assertVisible(loginPage.usernameField());
        assertions.assertVisible(loginPage.passwordField());
        assertions.assertVisible(loginPage.submitButton());

        stepRecorder.step("Login with valid credentials");
        loginPage.login("tomsmith", "SuperSecretPassword!");

        stepRecorder.step("Verify secure-area success message");
        assertions.assertVisible(loginPage.flashMessage());
        assertions.assertTextContains(loginPage.flashMessage(), "You logged into a secure area!");
    }
}
