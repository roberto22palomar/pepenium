package io.github.roberto22palomar.pepenium.smoke.web;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumSteps;

final class WebLoginFlow {

    @PepeniumInject
    private WebLoginPage loginPage;

    @PepeniumInject
    private PepeniumSteps stepRecorder;

    void loginWithValidCredentials() {
        stepRecorder.step("Open login page");
        loginPage.waitUntilLoaded();

        stepRecorder.step("Login with valid credentials");
        loginPage.login("tomsmith", "SuperSecretPassword!");

        stepRecorder.step("Verify secure-area success message");
        loginPage.assertSuccessMessageVisible();
    }
}
