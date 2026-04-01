package io.github.roberto22palomar.pepenium.tests.myProjectExample.ios;

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
@PepeniumTest(target = TestTarget.IOS_WEB, profile = "browserstack-ios-web")
public class ExampleIOSWebTest {

    @PepeniumInject
    private WebDriver driver;

    @PepeniumInject
    private ExampleAuthenticationFlow flow;

    @Test
    void basicNavigationFlow_shouldRun() {
        String baseUrl = System.getenv().getOrDefault("PEPENIUM_BASE_URL", "https://the-internet.herokuapp.com/login");
        String username = System.getenv().getOrDefault("PEPENIUM_WEB_USERNAME", "tomsmith");
        String password = System.getenv().getOrDefault("PEPENIUM_WEB_PASSWORD", "SuperSecretPassword!");
        driver.get(baseUrl);
        flow.runSuccessfulLoginAndDropdownSelection(baseUrl, username, password);
        log.info("Example iOS web flow finished");
    }
}
