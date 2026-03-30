package io.github.roberto22palomar.pepenium.core.execution;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import lombok.Builder;
import lombok.Getter;
import org.openqa.selenium.Capabilities;

import java.net.URL;

@Getter
@Builder(toBuilder = true)
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "The owned Appium service is intentionally passed through as a non-copyable runtime handle."
)
public class DriverRequest {
    private final DriverType driverType;
    private final URL serverUrl;
    private final Capabilities capabilities;
    private final AppiumDriverLocalService ownedService;
    private final String description;
    private final TestTarget target;
    private final String executionProfileId;
    private final String executionProfileDescription;
}
