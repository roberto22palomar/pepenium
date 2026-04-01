package io.github.roberto22palomar.pepenium.tests.myProjectExample.web;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumTest;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.plugandplay.PlugAndPlayAuthenticationFlow;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

@Slf4j
@Tag("pepenium-example")
@PepeniumTest(target = TestTarget.WEB_DESKTOP)
public class ExampleDesktopWebPlugAndPlayTest {

    @PepeniumInject
    private WebDriver driver;

    @PepeniumInject
    private PlugAndPlayAuthenticationFlow flow;

    @Test
    void basicNavigationFlow_shouldRunWithoutBaseTest() {
        String baseUrl = System.getenv().getOrDefault("PEPENIUM_BASE_URL", "https://the-internet.herokuapp.com/login");
        String username = System.getenv().getOrDefault("PEPENIUM_WEB_USERNAME", "tomsmith");
        String password = System.getenv().getOrDefault("PEPENIUM_WEB_PASSWORD", "SuperSecretPassword!");

        driver.get(baseUrl);
        flow.runSuccessfulLogin(username, password);

        log.info("Plug-and-play desktop web flow finished");
    }
}
