package io.github.roberto22palomar.pepenium.core.configs.local.android;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.github.roberto22palomar.pepenium.core.config.validation.ConfigValidationSupport;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Function;

public class AndroidConfigLocal implements DriverConfig {

    private final Function<String, String> env;

    public AndroidConfigLocal() {
        this(System::getenv);
    }

    AndroidConfigLocal(Function<String, String> env) {
        this.env = env;
    }

    @Override
    public DriverRequest createRequest() throws Exception {
        String appiumUrl = envOrDefault("APPIUM_URL", "http://localhost:4723");
        String udid = envOrDefault("ANDROID_UDID", "emulator-5554");
        String deviceName = envOrDefault("ANDROID_DEVICE_NAME", "Android Device");
        String platformName = envOrDefault("APPIUM_PLATFORM_NAME", "Android");
        String automationName = envOrDefault("APPIUM_AUTOMATION_NAME", "UiAutomator2");
        String platformVersion = envValue("APPIUM_PLATFORM_VERSION");
        var serverUrl = ConfigValidationSupport.requireUrl(
                appiumUrl,
                "APPIUM_URL",
                "Use a valid Appium server URL such as http://localhost:4723."
        );

        UiAutomator2Options opts = new UiAutomator2Options()
                .setPlatformName(platformName)
                .setAutomationName(automationName)
                .setDeviceName(deviceName)
                .setUdid(udid)
                .setNewCommandTimeout(Duration.ofSeconds(longEnvOrDefault("APPIUM_NEW_COMMAND_TIMEOUT", 300)))
                .setAutoGrantPermissions(booleanEnvOrDefault("APPIUM_AUTO_GRANT_PERMISSIONS", true))
                .setNoReset(booleanEnvOrDefault("APPIUM_NO_RESET", false))
                .setFullReset(booleanEnvOrDefault("APPIUM_FULL_RESET", false));

        if (notBlank(platformVersion)) {
            opts.setPlatformVersion(platformVersion);
        }

        applyBooleanCapability(opts, "APPIUM_DONT_STOP_APP_ON_RESET", "appium:dontStopAppOnReset");
        applyBooleanCapability(opts, "APPIUM_SKIP_DEVICE_INITIALIZATION", "appium:skipDeviceInitialization");
        applyBooleanCapability(opts, "APPIUM_SKIP_SERVER_INSTALLATION", "appium:skipServerInstallation");
        applyBooleanCapability(opts, "APPIUM_IGNORE_HIDDEN_API_POLICY_ERROR", "appium:ignoreHiddenApiPolicyError");
        applyBooleanCapability(opts, "APPIUM_AUTO_LAUNCH", "appium:autoLaunch");
        applyLongCapability(opts, "APPIUM_ADB_EXEC_TIMEOUT", "appium:adbExecTimeout");
        applyLongCapability(opts, "APPIUM_UIAUTOMATOR2_SERVER_LAUNCH_TIMEOUT", "appium:uiautomator2ServerLaunchTimeout");
        applyLongCapability(opts, "APPIUM_UIAUTOMATOR2_SERVER_INSTALL_TIMEOUT", "appium:uiautomator2ServerInstallTimeout");
        applyLongCapability(opts, "APPIUM_ANDROID_INSTALL_TIMEOUT", "appium:androidInstallTimeout");

        String appPath = env.apply("APP_PATH");
        if (notBlank(appPath)) {
            opts.setApp(ConfigValidationSupport.requireExistingFile(
                    stripQuotes(appPath),
                    "APP_PATH",
                    "Point it to a valid APK when running local Android native tests."
            ));
        }

        String appPackage = env.apply("APP_PACKAGE");
        String appActivity = env.apply("APP_ACTIVITY");
        if (notBlank(appPackage) && notBlank(appActivity)) {
            opts.setAppPackage(appPackage);
            opts.setAppActivity(appActivity);
        }

        String appWaitPackage = envValue("APP_WAIT_PACKAGE");
        if (notBlank(appWaitPackage)) {
            opts.setCapability("appium:appWaitPackage", appWaitPackage);
        }

        String appWaitActivity = envValue("APP_WAIT_ACTIVITY");
        if (notBlank(appWaitActivity)) {
            opts.setCapability("appium:appWaitActivity", appWaitActivity);
        }

        ConfigValidationSupport.requireAtLeastOneFilled(
                "Local Android native app configuration",
                Arrays.asList(appPath, appPackage),
                "Provide APP_PATH, or provide both APP_PACKAGE and APP_ACTIVITY."
        );
        if (notBlank(appPackage) || notBlank(appActivity)) {
            ConfigValidationSupport.requireAllFilled(
                    "Local Android native package/activity configuration",
                    Arrays.asList(appPackage, appActivity),
                    "APP_PACKAGE and APP_ACTIVITY must be provided together."
            );
        }

        return DriverRequest.builder()
                .driverType(DriverType.ANDROID_APPIUM)
                .serverUrl(serverUrl)
                .capabilities(opts)
                .description("Local Android native app")
                .build();
    }

    private String envOrDefault(String key, String defaultValue) {
        String value = envValue(key);
        return value == null ? defaultValue : value;
    }

    private String envValue(String key) {
        String value = env.apply(key);
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private boolean booleanEnvOrDefault(String key, boolean defaultValue) {
        String value = envValue(key);
        return value == null ? defaultValue : parseBoolean(key, value);
    }

    private long longEnvOrDefault(String key, long defaultValue) {
        String value = envValue(key);
        return value == null ? defaultValue : parseLong(key, value);
    }

    private void applyBooleanCapability(UiAutomator2Options options, String envKey, String capabilityName) {
        String value = envValue(envKey);
        if (value != null) {
            options.setCapability(capabilityName, parseBoolean(envKey, value));
        }
    }

    private void applyLongCapability(UiAutomator2Options options, String envKey, String capabilityName) {
        String value = envValue(envKey);
        if (value != null) {
            options.setCapability(capabilityName, parseLong(envKey, value));
        }
    }

    private long parseLong(String key, String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(key + " must be a valid integer value.", ex);
        }
    }

    private boolean parseBoolean(String key, String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalStateException(key + " must be either 'true' or 'false'.");
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String stripQuotes(String value) {
        return value == null ? null : value.replace("\"", "").trim();
    }
}
