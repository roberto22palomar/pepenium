package io.github.roberto22palomar.pepenium.smoke.custom;

import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import io.github.roberto22palomar.pepenium.core.execution.ExecutionProfile;
import io.github.roberto22palomar.pepenium.core.execution.ExecutionProfileProvider;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URI;
import java.util.Collection;
import java.util.List;

public final class TeamGridProfileProvider implements ExecutionProfileProvider {

    @Override
    public Collection<ExecutionProfile> profiles() {
        return List.of(new ExecutionProfile(
                "team-grid-web",
                TestTarget.WEB_DESKTOP,
                "Consumer-owned Selenium Grid profile",
                TeamGridConfig::new
        ));
    }

    private static final class TeamGridConfig implements DriverConfig {

        @Override
        public DriverRequest createRequest() throws Exception {
            String gridUrl = System.getProperty("team.grid.url", "http://localhost:4444/wd/hub");
            return DriverRequest.builder()
                    .driverType(DriverType.REMOTE_WEB)
                    .serverUrl(URI.create(gridUrl).toURL())
                    .capabilities(new ChromeOptions())
                    .description("Consumer-owned Selenium Grid")
                    .build();
        }
    }
}
