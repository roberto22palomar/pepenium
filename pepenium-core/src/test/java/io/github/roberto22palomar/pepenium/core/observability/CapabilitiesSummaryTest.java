package io.github.roberto22palomar.pepenium.core.observability;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.MutableCapabilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CapabilitiesSummaryTest {

    @Test
    void summarizeUsesFocusedKeysWhenPresent() {
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("appium:deviceName", "Pixel 9");
        capabilities.setCapability("appium:appPackage", "io.github.example");
        capabilities.setCapability("customCapability", "ignored");

        String summary = CapabilitiesSummary.summarize(capabilities);

        assertEquals(
                "platformName=ANDROID, appium:deviceName=Pixel 9, appium:appPackage=io.github.example",
                summary
        );
    }

    @Test
    void describeIncludesAllCapabilitiesInStableOrderAndRedactsSensitiveValues() {
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("bstack:options", "{deviceName=Pixel 9}");
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("password", "super-secret");
        capabilities.setCapability("accessKey", "abc123");

        String description = CapabilitiesSummary.describe(capabilities);

        assertTrue(description.contains("accessKey=***"));
        assertTrue(description.contains("password=***"));
        assertTrue(description.contains("platformName=ANDROID"));
        assertTrue(description.contains("bstack:options={deviceName=Pixel 9}"));
        assertTrue(description.indexOf("accessKey=***") < description.indexOf("platformName=ANDROID"));
        assertTrue(description.indexOf("bstack:options={deviceName=Pixel 9}") < description.indexOf("platformName=ANDROID"));
        assertTrue(description.indexOf("password=***") < description.indexOf("platformName=ANDROID"));
    }
}
