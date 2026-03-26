package io.github.roberto22palomar.pepenium.smoke.web;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.BaseTest;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb;
import org.junit.jupiter.api.Test;

class WebPublicApiSmokeTest extends BaseTest {

    @Override
    protected TestTarget getTarget() {
        return TestTarget.WEB_DESKTOP;
    }

    @Override
    protected boolean useAutomaticLifecycle() {
        return false;
    }

    @Test
    void publicWebAuthoringApiCompilesForExternalConsumers() {
        ActionsWeb actions = new ActionsWeb(driver);
        AssertionsWeb assertions = new AssertionsWeb(driver);
        WebLoginPage loginPage = new WebLoginPage(actions, assertions);
        WebLoginFlow flow = new WebLoginFlow(loginPage, this::step);

        flow.loginWithValidCredentials();
    }
}
