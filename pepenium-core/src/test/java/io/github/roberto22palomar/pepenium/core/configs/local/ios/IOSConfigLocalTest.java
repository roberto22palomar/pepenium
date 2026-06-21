package io.github.roberto22palomar.pepenium.core.configs.local.ios;

import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IOSConfigLocalTest {

    @TempDir
    Path tempDir;

    @Test
    void createsNativeRequestFromLocalIosSettings() throws Exception {
        Path app = Files.createDirectory(tempDir.resolve("Example.app"));
        Map<String, String> values = new HashMap<>();
        values.put("APPIUM_URL", "http://127.0.0.1:4723");
        values.put("IOS_DEVICE_NAME", "iPhone 16 Pro");
        values.put("IOS_UDID", "simulator-id");
        values.put("IOS_PLATFORM_VERSION", "18.0");
        values.put("IOS_APP_PATH", app.toString());

        DriverRequest request = new IOSConfigLocal(values::get).createRequest();

        assertEquals(DriverType.IOS_APPIUM, request.getDriverType());
        assertEquals("http://127.0.0.1:4723", request.getServerUrl().toString());
        assertEquals("iPhone 16 Pro", request.getCapabilities().getCapability("appium:deviceName"));
        assertEquals("simulator-id", request.getCapabilities().getCapability("appium:udid"));
        assertEquals("18.0", request.getCapabilities().getCapability("appium:platformVersion"));
        assertEquals(app.toString(), request.getCapabilities().getCapability("appium:app"));
    }

    @Test
    void supportsInstalledAppsByBundleId() throws Exception {
        DriverRequest request = new IOSConfigLocal(Map.of("IOS_BUNDLE_ID", "com.example.app")::get)
                .createRequest();

        assertEquals("com.example.app", request.getCapabilities().getCapability("appium:bundleId"));
    }

    @Test
    void requiresAnAppOrBundleId() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> new IOSConfigLocal(key -> null).createRequest()
        );

        assertTrue(error.getMessage().contains("IOS_APP_PATH"));
        assertTrue(error.getMessage().contains("IOS_BUNDLE_ID"));
    }
}
