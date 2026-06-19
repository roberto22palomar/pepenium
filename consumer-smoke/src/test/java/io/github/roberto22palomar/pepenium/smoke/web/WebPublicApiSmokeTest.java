package io.github.roberto22palomar.pepenium.smoke.web;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumTest;
import io.github.roberto22palomar.pepenium.toolkit.actions.WebActions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@PepeniumTest(target = TestTarget.WEB_DESKTOP)
class WebPublicApiSmokeTest {

    @PepeniumInject
    private WebDriver driver;

    @PepeniumInject
    private WebActions actions;

    @PepeniumInject
    private WebLoginFlow flow;

    @Test
    void publicWebAuthoringApiCompilesForExternalConsumers() {
        if (driver != null && flow != null) {
            flow.loginWithValidCredentials();
        }
        if (actions != null) {
            actions.isElementPresent(By.id("username"));
            actions.waitGone(By.cssSelector(".toast"));
        }
    }
}
