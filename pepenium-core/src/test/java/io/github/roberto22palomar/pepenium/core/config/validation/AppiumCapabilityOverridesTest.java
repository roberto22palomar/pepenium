package io.github.roberto22palomar.pepenium.core.config.validation;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppiumCapabilityOverridesTest {

    @Test
    void applyAndroidSupportsGenericTypedCapabilities() {
        UiAutomator2Options options = new UiAutomator2Options();

        AppiumCapabilityOverrides.applyAndroid(Map.of(
                "PEPENIUM_APPIUM_CAPABILITIES",
                "customTimeout=45000;appium:settings[mjpegScalingFactor]=25;vendor:flag=true;ratio=1.5"
        )::get, options);

        assertEquals(45000L, options.getCapability("appium:customTimeout"));
        assertEquals(25L, options.getCapability("appium:settings[mjpegScalingFactor]"));
        assertEquals(Boolean.TRUE, options.getCapability("vendor:flag"));
        assertEquals(1.5d, options.getCapability("appium:ratio"));
    }

    @Test
    void applyIosKeepsW3cCapabilityNamesUnprefixed() {
        XCUITestOptions options = new XCUITestOptions();

        AppiumCapabilityOverrides.applyIos(Map.of(
                "PEPENIUM_APPIUM_CAPABILITIES",
                "platformName=iOS;browserName=Safari;includeSafariInWebviews=true"
        )::get, options);

        assertEquals("IOS", String.valueOf(options.getCapability("platformName")));
        assertEquals("Safari", options.getCapability("browserName"));
        assertEquals(Boolean.TRUE, options.getCapability("appium:includeSafariInWebviews"));
    }

    @Test
    void genericCapabilitiesRejectMalformedEntries() {
        UiAutomator2Options options = new UiAutomator2Options();

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> AppiumCapabilityOverrides.applyAndroid(
                        Map.of("PEPENIUM_APPIUM_CAPABILITIES", "bad-entry")::get,
                        options
                )
        );

        assertEquals(
                "PEPENIUM_APPIUM_CAPABILITIES entries must follow key=value format and be separated with ';'.",
                error.getMessage()
        );
    }
}
