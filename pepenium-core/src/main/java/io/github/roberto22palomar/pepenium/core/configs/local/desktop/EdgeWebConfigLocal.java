package io.github.roberto22palomar.pepenium.core.configs.local.desktop;

import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import org.openqa.selenium.edge.EdgeOptions;

public class EdgeWebConfigLocal implements DriverConfig {

    @Override
    public DriverRequest createRequest() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("start-maximized");

        return DriverRequest.builder()
                .driverType(DriverType.LOCAL_EDGE)
                .capabilities(options)
                .description("Local Edge desktop web")
                .build();
    }
}
