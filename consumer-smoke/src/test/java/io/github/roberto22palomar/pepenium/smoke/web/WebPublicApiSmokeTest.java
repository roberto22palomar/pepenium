package io.github.roberto22palomar.pepenium.smoke.web;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumTest;
import org.openqa.selenium.WebDriver;
import org.junit.jupiter.api.Test;

@PepeniumTest(target = TestTarget.WEB_DESKTOP)
class WebPublicApiSmokeTest {

    @PepeniumInject
    private WebDriver driver;

    @PepeniumInject
    private WebLoginFlow flow;

    @Test
    void publicWebAuthoringApiCompilesForExternalConsumers() {
        if (driver != null && flow != null) {
            flow.loginWithValidCredentials();
        }
    }
}
