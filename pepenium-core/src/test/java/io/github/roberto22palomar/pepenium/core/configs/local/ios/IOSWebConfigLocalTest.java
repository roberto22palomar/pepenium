package io.github.roberto22palomar.pepenium.core.configs.local.ios;

import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IOSWebConfigLocalTest {

    @Test
    void createsSafariRequestFromLocalIosSettings() throws Exception {
        Map<String, String> values = Map.of(
                "APPIUM_URL", "http://127.0.0.1:4723",
                "IOS_DEVICE_NAME", "iPhone 16 Pro",
                "IOS_UDID", "simulator-id",
                "IOS_PLATFORM_VERSION", "18.0"
        );

        DriverRequest request = new IOSWebConfigLocal(values::get).createRequest();

        assertEquals(DriverType.IOS_APPIUM, request.getDriverType());
        assertEquals("Safari", request.getCapabilities().getCapability("browserName"));
        assertEquals("iPhone 16 Pro", request.getCapabilities().getCapability("appium:deviceName"));
        assertEquals("simulator-id", request.getCapabilities().getCapability("appium:udid"));
        assertEquals("18.0", request.getCapabilities().getCapability("appium:platformVersion"));
        assertEquals("Local iOS mobile web", request.getDescription());
    }
}
