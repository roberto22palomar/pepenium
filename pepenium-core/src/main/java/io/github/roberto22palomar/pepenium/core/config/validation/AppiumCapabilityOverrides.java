package io.github.roberto22palomar.pepenium.core.config.validation;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.time.Duration;
import java.util.function.Function;

public final class AppiumCapabilityOverrides {

    private AppiumCapabilityOverrides() {
    }

    public static void applyAndroid(Function<String, String> env, UiAutomator2Options options) {
        applyString(env, "APPIUM_PLATFORM_NAME", options::setPlatformName);
        applyString(env, "APPIUM_AUTOMATION_NAME", options::setAutomationName);
        applyString(env, "APPIUM_PLATFORM_VERSION", options::setPlatformVersion);
        applyDurationSeconds(env, "APPIUM_NEW_COMMAND_TIMEOUT", options::setNewCommandTimeout);
        applyBoolean(env, "APPIUM_AUTO_GRANT_PERMISSIONS",
                value -> options.setCapability("appium:autoGrantPermissions", value));
        applyBoolean(env, "APPIUM_NO_RESET", options::setNoReset);
        applyBoolean(env, "APPIUM_FULL_RESET", options::setFullReset);
        applyBooleanCapability(env, options, "APPIUM_DONT_STOP_APP_ON_RESET", "appium:dontStopAppOnReset");
        applyBooleanCapability(env, options, "APPIUM_SKIP_DEVICE_INITIALIZATION", "appium:skipDeviceInitialization");
        applyBooleanCapability(env, options, "APPIUM_SKIP_SERVER_INSTALLATION", "appium:skipServerInstallation");
        applyBooleanCapability(env, options, "APPIUM_IGNORE_HIDDEN_API_POLICY_ERROR", "appium:ignoreHiddenApiPolicyError");
        applyBooleanCapability(env, options, "APPIUM_AUTO_LAUNCH", "appium:autoLaunch");
        applyLongCapability(env, options, "APPIUM_ADB_EXEC_TIMEOUT", "appium:adbExecTimeout");
        applyLongCapability(env, options, "APPIUM_UIAUTOMATOR2_SERVER_LAUNCH_TIMEOUT",
                "appium:uiautomator2ServerLaunchTimeout");
        applyLongCapability(env, options, "APPIUM_UIAUTOMATOR2_SERVER_INSTALL_TIMEOUT",
                "appium:uiautomator2ServerInstallTimeout");
        applyLongCapability(env, options, "APPIUM_ANDROID_INSTALL_TIMEOUT", "appium:androidInstallTimeout");
        applyStringCapability(env, options, "APP_WAIT_PACKAGE", "appium:appWaitPackage");
        applyStringCapability(env, options, "APP_WAIT_ACTIVITY", "appium:appWaitActivity");
    }

    public static void applyIos(Function<String, String> env, XCUITestOptions options) {
        applyString(env, "APPIUM_PLATFORM_NAME", options::setPlatformName);
        applyString(env, "APPIUM_AUTOMATION_NAME", options::setAutomationName);
        applyString(env, "APPIUM_PLATFORM_VERSION", options::setPlatformVersion);
        applyDurationSeconds(env, "APPIUM_NEW_COMMAND_TIMEOUT", options::setNewCommandTimeout);
        applyBoolean(env, "APPIUM_NO_RESET", options::setNoReset);
        applyBoolean(env, "APPIUM_FULL_RESET", options::setFullReset);
        applyBoolean(env, "APPIUM_AUTO_ACCEPT_ALERTS", options::setAutoAcceptAlerts);
        applyDurationSeconds(env, "APPIUM_WDA_LAUNCH_TIMEOUT", options::setWdaLaunchTimeout);
        applyDurationSeconds(env, "APPIUM_WDA_CONNECTION_TIMEOUT", options::setWdaConnectionTimeout);
        applyBooleanCapability(env, options, "APPIUM_AUTO_LAUNCH", "appium:autoLaunch");
    }

    private static void applyString(Function<String, String> env, String key, java.util.function.Consumer<String> setter) {
        String value = envValue(env, key);
        if (value != null) {
            setter.accept(value);
        }
    }

    private static void applyDurationSeconds(Function<String, String> env,
                                             String key,
                                             java.util.function.Consumer<Duration> setter) {
        String value = envValue(env, key);
        if (value != null) {
            setter.accept(Duration.ofSeconds(parseLong(key, value)));
        }
    }

    private static void applyBoolean(Function<String, String> env,
                                     String key,
                                     java.util.function.Consumer<Boolean> setter) {
        String value = envValue(env, key);
        if (value != null) {
            setter.accept(parseBoolean(key, value));
        }
    }

    private static void applyStringCapability(Function<String, String> env,
                                              Object options,
                                              String envKey,
                                              String capabilityName) {
        String value = envValue(env, envKey);
        if (value != null) {
            if (options instanceof UiAutomator2Options) {
                ((UiAutomator2Options) options).setCapability(capabilityName, value);
            } else {
                ((XCUITestOptions) options).setCapability(capabilityName, value);
            }
        }
    }

    private static void applyBooleanCapability(Function<String, String> env,
                                               Object options,
                                               String envKey,
                                               String capabilityName) {
        String value = envValue(env, envKey);
        if (value != null) {
            boolean parsed = parseBoolean(envKey, value);
            if (options instanceof UiAutomator2Options) {
                ((UiAutomator2Options) options).setCapability(capabilityName, parsed);
            } else {
                ((XCUITestOptions) options).setCapability(capabilityName, parsed);
            }
        }
    }

    private static void applyLongCapability(Function<String, String> env,
                                            Object options,
                                            String envKey,
                                            String capabilityName) {
        String value = envValue(env, envKey);
        if (value != null) {
            long parsed = parseLong(envKey, value);
            if (options instanceof UiAutomator2Options) {
                ((UiAutomator2Options) options).setCapability(capabilityName, parsed);
            } else {
                ((XCUITestOptions) options).setCapability(capabilityName, parsed);
            }
        }
    }

    private static String envValue(Function<String, String> env, String key) {
        String value = env.apply(key);
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static long parseLong(String key, String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(key + " must be a valid integer value.", ex);
        }
    }

    private static boolean parseBoolean(String key, String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalStateException(key + " must be either 'true' or 'false'.");
    }
}
