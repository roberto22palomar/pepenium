package io.github.roberto22palomar.pepenium.core.config.validation;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;

import java.util.function.Function;

public final class WebCapabilityOverrides {

    private WebCapabilityOverrides() {
    }

    public static void applyChrome(Function<String, String> env, ChromeOptions options) {
        applyCommon(env, options, BrowserKind.CHROMIUM);
    }

    public static void applyFirefox(Function<String, String> env, FirefoxOptions options) {
        applyCommon(env, options, BrowserKind.FIREFOX);
    }

    public static void applyEdge(Function<String, String> env, EdgeOptions options) {
        applyCommon(env, options, BrowserKind.CHROMIUM);
    }

    private static void applyCommon(Function<String, String> env,
                                    AbstractDriverOptions<?> options,
                                    BrowserKind browserKind) {
        applyBoolean(env, "PEPENIUM_WEB_ACCEPT_INSECURE_CERTS", options::setAcceptInsecureCerts);
        applyString(env, "PEPENIUM_WEB_BROWSER_VERSION", options::setBrowserVersion);
        applyBinary(env, options, browserKind);
        applyPageLoadStrategy(env, options);
        applyArguments(env, options, browserKind);
        applyHeadless(env, options, browserKind);
        applyGenericCapabilities(env, options);
    }

    private static void applyPageLoadStrategy(Function<String, String> env, AbstractDriverOptions<?> options) {
        String value = envValue(env, "PEPENIUM_WEB_PAGE_LOAD_STRATEGY");
        if (value == null) {
            return;
        }
        try {
            options.setPageLoadStrategy(PageLoadStrategy.fromString(value));
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "PEPENIUM_WEB_PAGE_LOAD_STRATEGY must be one of 'normal', 'eager' or 'none'.", ex);
        }
    }

    private static void applyBinary(Function<String, String> env,
                                    AbstractDriverOptions<?> options,
                                    BrowserKind browserKind) {
        String value = envValue(env, "PEPENIUM_WEB_BINARY_PATH");
        if (value != null) {
            browserKind.setBinary(options, value);
        }
    }

    private static void applyArguments(Function<String, String> env,
                                       AbstractDriverOptions<?> options,
                                       BrowserKind browserKind) {
        String value = envValue(env, "PEPENIUM_WEB_ARGS");
        if (value == null) {
            return;
        }
        for (String rawArg : value.split(";")) {
            String arg = rawArg == null ? null : rawArg.trim();
            if (arg == null || arg.isBlank()) {
                continue;
            }
            browserKind.addArgument(options, arg);
        }
    }

    private static void applyHeadless(Function<String, String> env,
                                      AbstractDriverOptions<?> options,
                                      BrowserKind browserKind) {
        String value = envValue(env, "PEPENIUM_WEB_HEADLESS");
        if (value == null || !parseBoolean("PEPENIUM_WEB_HEADLESS", value)) {
            return;
        }
        browserKind.addHeadlessArgument(options);
    }

    private static void applyGenericCapabilities(Function<String, String> env, AbstractDriverOptions<?> options) {
        String value = envValue(env, "PEPENIUM_WEB_CAPABILITIES");
        if (value == null) {
            return;
        }
        for (String rawEntry : value.split(";")) {
            String entry = rawEntry == null ? null : rawEntry.trim();
            if (entry == null || entry.isBlank()) {
                continue;
            }
            int separator = entry.indexOf('=');
            if (separator <= 0 || separator == entry.length() - 1) {
                throw new IllegalStateException(
                        "PEPENIUM_WEB_CAPABILITIES entries must follow key=value format and be separated with ';'.");
            }
            String key = entry.substring(0, separator).trim();
            String rawValue = entry.substring(separator + 1).trim();
            if (key.isBlank() || rawValue.isBlank()) {
                throw new IllegalStateException(
                        "PEPENIUM_WEB_CAPABILITIES entries must follow key=value format and be separated with ';'.");
            }
            options.setCapability(key, parseScalar(rawValue));
        }
    }

    private static void applyString(Function<String, String> env,
                                    String key,
                                    java.util.function.Consumer<String> setter) {
        String value = envValue(env, key);
        if (value != null) {
            setter.accept(value);
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

    private static String envValue(Function<String, String> env, String key) {
        String value = env.apply(key);
        return (value == null || value.isBlank()) ? null : value.trim();
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

    private static Object parseScalar(String rawValue) {
        String value = rawValue.trim();
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
        }
        return value;
    }

    private enum BrowserKind {
        CHROMIUM {
            @Override
            void addArgument(AbstractDriverOptions<?> options, String argument) {
                if (options instanceof ChromeOptions) {
                    ((ChromeOptions) options).addArguments(argument);
                    return;
                }
                ((EdgeOptions) options).addArguments(argument);
            }

            @Override
            void addHeadlessArgument(AbstractDriverOptions<?> options) {
                addArgument(options, "--headless=new");
            }

            @Override
            void setBinary(AbstractDriverOptions<?> options, String binaryPath) {
                if (options instanceof ChromeOptions) {
                    ((ChromeOptions) options).setBinary(binaryPath);
                    return;
                }
                ((EdgeOptions) options).setBinary(binaryPath);
            }
        },
        FIREFOX {
            @Override
            void addArgument(AbstractDriverOptions<?> options, String argument) {
                ((FirefoxOptions) options).addArguments(argument);
            }

            @Override
            void addHeadlessArgument(AbstractDriverOptions<?> options) {
                addArgument(options, "-headless");
            }

            @Override
            void setBinary(AbstractDriverOptions<?> options, String binaryPath) {
                ((FirefoxOptions) options).setBinary(binaryPath);
            }
        };

        abstract void addArgument(AbstractDriverOptions<?> options, String argument);

        abstract void addHeadlessArgument(AbstractDriverOptions<?> options);

        abstract void setBinary(AbstractDriverOptions<?> options, String binaryPath);
    }
}
