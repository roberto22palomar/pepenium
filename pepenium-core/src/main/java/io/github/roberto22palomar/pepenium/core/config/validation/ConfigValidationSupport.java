package io.github.roberto22palomar.pepenium.core.config.validation;

import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfigDesktop;
import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfigMobile;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ConfigValidationSupport {

    private ConfigValidationSupport() {
    }

    public static String requireNonBlank(String value, String label, String hint) {
        if (value == null || value.isBlank()) {
            throw invalid(label + " is required. " + hint);
        }
        return value.trim();
    }

    public static String requireExistingFile(String value, String label, String hint) {
        String resolved = requireNonBlank(value, label, hint);
        if (!Files.exists(Path.of(resolved))) {
            throw invalid(label + " points to a path that does not exist: '" + resolved + "'. " + hint);
        }
        return resolved;
    }

    public static URL requireUrl(String value, String label, String hint) {
        String resolved = requireNonBlank(value, label, hint);
        try {
            return new URL(resolved);
        } catch (MalformedURLException e) {
            throw invalid(label + " must be a valid URL but was '" + resolved + "'. " + hint, e);
        }
    }

    public static void requireAtLeastOneFilled(String label, List<String> values, String hint) {
        boolean anyFilled = values.stream().anyMatch(value -> value != null && !value.isBlank());
        if (!anyFilled) {
            throw invalid(label + " requires at least one valid option. " + hint);
        }
    }

    public static void requireAllFilled(String label, List<String> values, String hint) {
        boolean allFilled = values.stream().allMatch(value -> value != null && !value.isBlank());
        if (!allFilled) {
            throw invalid(label + " requires all related values. " + hint);
        }
    }

    public static BrowserStackConfig validateBrowserStackAppConfig(BrowserStackConfig config, String yamlPath) {
        if (config == null) {
            throw invalid("BrowserStack YAML '" + yamlPath + "' did not contain a valid document.");
        }

        requireNonBlank(config.getUserName(), "BrowserStack userName",
                "Set it in '" + yamlPath + "'.");
        requireNonBlank(config.getAccessKey(), "BrowserStack accessKey",
                "Set it in '" + yamlPath + "'.");
        requireNonBlank(config.getApp(), "BrowserStack app",
                "Native BrowserStack configs must define an app id or uploaded app reference in '" + yamlPath + "'.");

        validateMobileAppPlatforms(config.getPlatforms(), yamlPath);
        return config;
    }

    public static BrowserStackConfigMobile validateBrowserStackMobileWebConfig(BrowserStackConfigMobile config, String yamlPath) {
        if (config == null) {
            throw invalid("BrowserStack YAML '" + yamlPath + "' did not contain a valid document.");
        }

        requireNonBlank(config.getUserName(), "BrowserStack userName",
                "Set it in '" + yamlPath + "'.");
        requireNonBlank(config.getAccessKey(), "BrowserStack accessKey",
                "Set it in '" + yamlPath + "'.");

        List<BrowserStackConfigMobile.Platform> platforms = config.getPlatforms();
        if (platforms == null || platforms.isEmpty()) {
            throw invalid("BrowserStack YAML '" + yamlPath + "' must declare at least one platform.");
        }

        for (int i = 0; i < platforms.size(); i++) {
            BrowserStackConfigMobile.Platform platform = platforms.get(i);
            if (platform == null) {
                throw invalid("BrowserStack YAML '" + yamlPath + "' contains a null platform at index " + i + ".");
            }
            requireNonBlank(platform.getDeviceName(), "BrowserStack platform[" + i + "].deviceName",
                    "Each mobile-web platform in '" + yamlPath + "' must define a deviceName.");
            requireNonBlank(platform.getOsVersion(), "BrowserStack platform[" + i + "].osVersion",
                    "Each mobile-web platform in '" + yamlPath + "' must define an osVersion.");
        }
        return config;
    }

    public static BrowserStackConfigDesktop validateBrowserStackDesktopConfig(BrowserStackConfigDesktop config, String yamlPath) {
        if (config == null) {
            throw invalid("BrowserStack YAML '" + yamlPath + "' did not contain a valid document.");
        }

        requireNonBlank(config.getUserName(), "BrowserStack userName",
                "Set it in '" + yamlPath + "'.");
        requireNonBlank(config.getAccessKey(), "BrowserStack accessKey",
                "Set it in '" + yamlPath + "'.");

        List<BrowserStackConfigDesktop.Platform> platforms = config.getPlatforms();
        if (platforms == null || platforms.isEmpty()) {
            throw invalid("BrowserStack YAML '" + yamlPath + "' must declare at least one platform.");
        }

        for (int i = 0; i < platforms.size(); i++) {
            BrowserStackConfigDesktop.Platform platform = platforms.get(i);
            if (platform == null) {
                throw invalid("BrowserStack YAML '" + yamlPath + "' contains a null platform at index " + i + ".");
            }
            requireNonBlank(platform.getOs(), "BrowserStack platform[" + i + "].os",
                    "Each desktop platform in '" + yamlPath + "' must define an os.");
            requireNonBlank(platform.getOsVersion(), "BrowserStack platform[" + i + "].osVersion",
                    "Each desktop platform in '" + yamlPath + "' must define an osVersion.");
            requireNonBlank(platform.getBrowserName(), "BrowserStack platform[" + i + "].browserName",
                    "Each desktop platform in '" + yamlPath + "' must define a browserName.");
            requireNonBlank(platform.getBrowserVersion(), "BrowserStack platform[" + i + "].browserVersion",
                    "Each desktop platform in '" + yamlPath + "' must define a browserVersion.");
        }
        return config;
    }

    private static void validateMobileAppPlatforms(List<BrowserStackConfig.Platform> platforms, String yamlPath) {
        if (platforms == null || platforms.isEmpty()) {
            throw invalid("BrowserStack YAML '" + yamlPath + "' must declare at least one platform.");
        }

        for (int i = 0; i < platforms.size(); i++) {
            BrowserStackConfig.Platform platform = platforms.get(i);
            if (platform == null) {
                throw invalid("BrowserStack YAML '" + yamlPath + "' contains a null platform at index " + i + ".");
            }
            requireNonBlank(platform.getPlatformName(), "BrowserStack platform[" + i + "].platformName",
                    "Each native-mobile platform in '" + yamlPath + "' must define a platformName.");
            requireNonBlank(platform.getDeviceName(), "BrowserStack platform[" + i + "].deviceName",
                    "Each native-mobile platform in '" + yamlPath + "' must define a deviceName.");
            requireNonBlank(platform.getPlatformVersion(), "BrowserStack platform[" + i + "].platformVersion",
                    "Each native-mobile platform in '" + yamlPath + "' must define a platformVersion.");
        }
    }

    public static IllegalStateException invalid(String message) {
        return new IllegalStateException("Invalid Pepenium configuration: " + message);
    }

    public static IllegalStateException invalid(String message, Throwable cause) {
        return new IllegalStateException("Invalid Pepenium configuration: " + message, cause);
    }
}
