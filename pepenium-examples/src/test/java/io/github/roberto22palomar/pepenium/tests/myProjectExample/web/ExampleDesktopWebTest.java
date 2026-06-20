package io.github.roberto22palomar.pepenium.tests.myProjectExample.web;

import io.github.roberto22palomar.pepenium.core.config.PepeniumConfig;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumTest;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.flows.ExampleAuthenticationFlow;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

@Slf4j
@Tag("pepenium-example")
@PepeniumTest(target = TestTarget.WEB_DESKTOP)
public class ExampleDesktopWebTest {

    @PepeniumInject
    private WebDriver driver;

    @PepeniumInject
    private ExampleAuthenticationFlow flow;

    @Test
    void basicNavigationFlow_shouldRun() {
        String baseUrl = PepeniumConfig.getOrDefault(
                "PEPENIUM_BASE_URL", "https://the-internet.herokuapp.com/login");
        String username = PepeniumConfig.getOrDefault("PEPENIUM_WEB_USERNAME", "tomsmith");
        String password = PepeniumConfig.getOrDefault("PEPENIUM_WEB_PASSWORD", "SuperSecretPassword!");
        driver.get(baseUrl);
        flow.runSuccessfulLoginAndDropdownSelection(baseUrl, username, password);
        log.info("Example desktop web flow finished");
    }
}
