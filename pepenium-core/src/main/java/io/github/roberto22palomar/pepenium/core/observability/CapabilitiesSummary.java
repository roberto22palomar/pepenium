package io.github.roberto22palomar.pepenium.core.observability;

import org.openqa.selenium.Capabilities;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

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

    public static String describe(Capabilities capabilities) {
        if (capabilities == null) {
            return "none";
        }

        Map<String, Object> all = capabilities.asMap();
        if (all.isEmpty()) {
            return "present";
        }

        Map<String, Object> ordered = new TreeMap<>(Comparator.naturalOrder());
        all.forEach((key, value) -> ordered.put(key, sanitizeValue(key, value)));

        StringJoiner joiner = new StringJoiner(", ");
        ordered.forEach((key, value) -> joiner.add(key + "=" + value));
        return joiner.toString();
    }

    private static Object sanitizeValue(String key, Object value) {
        if (value == null) {
            return null;
        }
        if (isSensitiveKey(key)) {
            return "***";
        }
        return value;
    }

    private static boolean isSensitiveKey(String key) {
        String normalized = key == null ? "" : key.toLowerCase();
        return normalized.contains("accesskey")
                || normalized.contains("password")
                || normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("apikey");
    }
}
