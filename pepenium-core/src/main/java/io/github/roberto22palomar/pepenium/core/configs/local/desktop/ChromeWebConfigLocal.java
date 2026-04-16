package io.github.roberto22palomar.pepenium.core.configs.local.desktop;

import io.github.roberto22palomar.pepenium.core.config.validation.WebCapabilityOverrides;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeWebConfigLocal implements DriverConfig {

    @Override
    public DriverRequest createRequest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");
        WebCapabilityOverrides.applyChrome(WebCapabilityOverrides::systemPropertyOrEnv, options);

        return DriverRequest.builder()
                .driverType(DriverType.LOCAL_CHROME)
                .capabilities(options)
                .description("Local Chrome desktop web")
                .build();
    }
}
