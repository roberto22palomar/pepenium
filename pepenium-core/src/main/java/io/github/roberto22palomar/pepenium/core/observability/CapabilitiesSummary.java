package io.github.roberto22palomar.pepenium.core.observability;

import org.openqa.selenium.Capabilities;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class CapabilitiesSummary {

    private static final String[] KEYS = {
            "platformName",
            "browserName",
            "automationName",
            "deviceName",
            "appium:deviceName",
            "platformVersion",
            "appium:platformVersion",
            "udid",
            "appium:udid",
            "appPackage",
            "appium:appPackage",
            "appActivity",
            "appium:appActivity",
            "bundleId",
            "appium:bundleId"
    };

    private CapabilitiesSummary() {
    }

    public static String summarize(Capabilities capabilities) {
        if (capabilities == null) {
            return "none";
        }

        Map<String, Object> selected = new LinkedHashMap<>();
        for (String key : KEYS) {
            Object value = capabilities.getCapability(key);
            if (value != null) {
                selected.put(key, value);
            }
        }

        if (selected.isEmpty()) {
            return "present";
        }

        StringJoiner joiner = new StringJoiner(", ");
        selected.forEach((key, value) -> joiner.add(key + "=" + value));
        return joiner.toString();
    }
}
