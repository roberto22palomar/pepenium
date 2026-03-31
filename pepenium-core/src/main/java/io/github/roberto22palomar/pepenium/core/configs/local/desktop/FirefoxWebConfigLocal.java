package io.github.roberto22palomar.pepenium.core.configs.local.desktop;

import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import org.openqa.selenium.firefox.FirefoxOptions;

public class FirefoxWebConfigLocal implements DriverConfig {

    @Override
    public DriverRequest createRequest() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");

        return DriverRequest.builder()
                .driverType(DriverType.LOCAL_FIREFOX)
                .capabilities(options)
                .description("Local Firefox desktop web")
                .build();
    }
}
