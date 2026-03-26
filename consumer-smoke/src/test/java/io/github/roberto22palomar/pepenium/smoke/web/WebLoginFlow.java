package io.github.roberto22palomar.pepenium.smoke.web;

import java.util.function.Consumer;

final class WebLoginFlow {

    private final WebLoginPage loginPage;
    private final Consumer<String> stepRecorder;

    WebLoginFlow(WebLoginPage loginPage, Consumer<String> stepRecorder) {
        this.loginPage = loginPage;
        this.stepRecorder = stepRecorder;
    }

    void loginWithValidCredentials() {
        stepRecorder.accept("Open login page");
        loginPage.waitUntilLoaded();

        stepRecorder.accept("Login with valid credentials");
        loginPage.login("tomsmith", "SuperSecretPassword!");

        stepRecorder.accept("Verify secure-area success message");
        loginPage.assertSuccessMessageVisible();
    }
}
