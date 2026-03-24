package io.github.roberto22palomar.pepenium.core;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import lombok.Builder;
import lombok.Getter;
import org.openqa.selenium.Capabilities;

import java.net.URL;

@Getter
@Builder
public class DriverRequest {
    private final DriverType driverType;
    private final URL serverUrl;
    private final Capabilities capabilities;
    private final AppiumDriverLocalService ownedService;
    private final String description;
}
