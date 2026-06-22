package io.github.roberto22palomar.pepenium.core.config;

import io.github.roberto22palomar.pepenium.core.observability.SensitiveDataSanitizer;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PepeniumConfig {

    private static final String DEFAULT_FILE = "pepenium.yml";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([A-Za-z_][A-Za-z0-9_]*)}");
    private static final ThreadLocal<String> ACTIVE_PROFILE = new ThreadLocal<>();
    private static final Map<String, String> KEY_PATHS = createKeyPaths();
    private static final Set<String> ROOT_KEYS = Set.of(
            "schemaVersion", "defaultProfile", "baseUrl", "credentials", "reporting", "logging", "timeouts",
            "capabilities", "settings", "profiles"
    );
    private static final Set<String> PROFILE_KEYS = Set.of(
            "serverUrl", "device", "app", "browser", "baseUrl", "credentials", "reporting",
            "logging", "timeouts", "capabilities", "settings"
    );

    private PepeniumConfig() {
    }

    public static void activateProfile(String profileId) {
        if (profileId == null || profileId.isBlank()) {
            ACTIVE_PROFILE.remove();
        } else {
            ACTIVE_PROFILE.set(profileId.trim());
        }
    }

    public static void clearActiveProfile() {
        ACTIVE_PROFILE.remove();
    }

    public static String get(String key) {
        String property = System.getProperty(toSystemPropertyKey(key));
        if (isBlank(property)) {
            property = System.getProperty(key);
        }
        if (!isBlank(property)) {
            return property.trim();
        }
        String environment = System.getenv(key);
        if (!isBlank(environment)) {
            return environment.trim();
        }
        return Holder.CONFIG.value(ACTIVE_PROFILE.get(), key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return isBlank(value) ? defaultValue : value;
    }

    public static String getDefaultProfile() {
        return Holder.CONFIG.defaultProfile();
    }

    public static Map<String, Object> getCapabilities() {
        return Holder.CONFIG.capabilities(ACTIVE_PROFILE.get());
    }

    /**
     * Returns the deeply merged open-ended settings for the active profile.
     *
     * <p>Values preserve their YAML types and the returned maps and lists are immutable.</p>
     *
     * @return resolved global and active-profile settings
     */
    public static Map<String, Object> getSettings() {
        return Holder.CONFIG.settings(ACTIVE_PROFILE.get());
    }

    public static void validateProfile(String profileId) {
        Holder.CONFIG.validateResolvedProfile(profileId);
    }

    /**
     * Validates a configuration file without creating a driver session.
     *
     * @param path configuration file to validate
     * @param profileId profile whose placeholders and provider rules must be resolved; when blank, the configured
     *                  default profile is used
     */
    public static void validate(Path path, String profileId) {
        Objects.requireNonNull(path, "Configuration path must not be null");
        ResolvedConfig config = load(path, true, System::getenv);
        String selectedProfile = isBlank(profileId) ? config.defaultProfile() : profileId.trim();
        config.validateResolvedProfile(selectedProfile);
    }

    /**
     * Validates a configuration file and its default profile without creating a driver session.
     *
     * @param path configuration file to validate
     */
    public static void validate(Path path) {
        validate(path, null);
    }

    static ResolvedConfig load(Path path, boolean explicit, Function<String, String> environment) {
        if (!Files.exists(path)) {
            if (explicit) {
                throw invalid("Configuration file does not exist: " + path.toAbsolutePath());
            }
            return ResolvedConfig.empty(environment);
        }
        try (InputStream input = Files.newInputStream(path)) {
            Object document = createYamlParser().load(input);
            if (document == null) {
                return ResolvedConfig.empty(environment);
            }
            if (!(document instanceof Map)) {
                throw invalid("Configuration root must be a YAML object in " + path.toAbsolutePath());
            }
            return ResolvedConfig.from((Map<?, ?>) document, environment, path);
        } catch (YAMLException error) {
            throw invalid("Could not parse YAML file " + path.toAbsolutePath() + ": "
                    + SensitiveDataSanitizer.sanitizeText(error.getMessage()));
        } catch (IOException error) {
            throw invalid("Could not read configuration file " + path.toAbsolutePath(), error);
        }
    }

    private static Yaml createYamlParser() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(50);
        options.setNestingDepthLimit(50);
        options.setCodePointLimit(3 * 1024 * 1024);
        return new Yaml(new SafeConstructor(options));
    }

    private static ResolvedConfig loadCurrent() {
        String propertyPath = System.getProperty("pepenium.config");
        String environmentPath = System.getenv("PEPENIUM_CONFIG");
        String configuredPath = !isBlank(propertyPath) ? propertyPath : environmentPath;
        boolean explicit = !isBlank(configuredPath);
        Path path = Path.of(explicit ? configuredPath.trim() : DEFAULT_FILE);
        return load(path, explicit, System::getenv);
    }

    private static Map<String, String> createKeyPaths() {
        Map<String, String> paths = new LinkedHashMap<>();
        paths.put("APPIUM_URL", "serverUrl");
        paths.put("ANDROID_UDID", "device.udid");
        paths.put("ANDROID_DEVICE_NAME", "device.name");
        paths.put("IOS_UDID", "device.udid");
        paths.put("IOS_DEVICE_NAME", "device.name");
        paths.put("IOS_PLATFORM_VERSION", "device.platformVersion");
        paths.put("APP_PATH", "app.path");
        paths.put("IOS_APP_PATH", "app.path");
        paths.put("IOS_BUNDLE_ID", "app.bundleId");
        paths.put("APP_PACKAGE", "app.package");
        paths.put("APP_ACTIVITY", "app.activity");
        paths.put("PEPENIUM_WEB_HEADLESS", "browser.headless");
        paths.put("PEPENIUM_WEB_ACCEPT_INSECURE_CERTS", "browser.acceptInsecureCerts");
        paths.put("PEPENIUM_WEB_PAGE_LOAD_STRATEGY", "browser.pageLoadStrategy");
        paths.put("PEPENIUM_WEB_BROWSER_VERSION", "browser.version");
        paths.put("PEPENIUM_WEB_BINARY_PATH", "browser.binary");
        paths.put("PEPENIUM_WEB_ARGS", "browser.arguments");
        paths.put("PEPENIUM_BASE_URL", "baseUrl");
        paths.put("PEPENIUM_WEB_USERNAME", "credentials.username");
        paths.put("PEPENIUM_WEB_PASSWORD", "credentials.password");
        paths.put("PEPENIUM_REPORT_DIR", "reporting.directory");
        paths.put("PEPENIUM_SCREENSHOT_PATH", "reporting.screenshotPath");
        paths.put("PEPENIUM_DETAIL_LOGGING", "logging.detailed");
        paths.put("PEPENIUM_STEP_TRACKER_LIMIT", "logging.stepLimit");
        paths.put("PEPENIUM_ACTION_TIMEOUT_SECONDS", "timeouts.action");
        paths.put("PEPENIUM_ACTION_LONG_TIMEOUT_SECONDS", "timeouts.longAction");
        paths.put("PEPENIUM_ASSERTION_TIMEOUT_SECONDS", "timeouts.assertion");
        paths.put("PEPENIUM_SESSION_CONNECT_TIMEOUT_SECONDS", "timeouts.sessionConnect");
        paths.put("PEPENIUM_SESSION_COMMAND_TIMEOUT_SECONDS", "timeouts.sessionCommand");
        return Collections.unmodifiableMap(paths);
    }

    private static String toSystemPropertyKey(String key) {
        return key.toLowerCase(Locale.ROOT).replace('_', '.');
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static IllegalStateException invalid(String message) {
        return new IllegalStateException("Invalid Pepenium configuration: " + message);
    }

    private static IllegalStateException invalid(String message, Throwable cause) {
        return new IllegalStateException("Invalid Pepenium configuration: " + message, cause);
    }

    private static final class Holder {
        private static final ResolvedConfig CONFIG = loadCurrent();
    }

    static final class ResolvedConfig {
        private final String defaultProfile;
        private final Map<String, Object> global;
        private final Map<String, Map<String, Object>> profiles;
        private final Function<String, String> environment;
        private final Path source;

        private ResolvedConfig(String defaultProfile,
                               Map<String, Object> global,
                               Map<String, Map<String, Object>> profiles,
                               Function<String, String> environment,
                               Path source) {
            this.defaultProfile = defaultProfile;
            this.global = global;
            this.profiles = profiles;
            this.environment = environment;
            this.source = source;
        }

        static ResolvedConfig empty(Function<String, String> environment) {
            return new ResolvedConfig(
                    null,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    environment,
                    Path.of(DEFAULT_FILE)
            );
        }

        static ResolvedConfig from(Map<?, ?> document, Function<String, String> environment, Path source) {
            validateDocument(document, source);
            String defaultProfile = scalar(document.get("defaultProfile"), "defaultProfile", environment, source);
            Map<String, Map<String, Object>> profiles = new LinkedHashMap<>();
            Object rawProfiles = document.get("profiles");
            if (rawProfiles != null) {
                if (!(rawProfiles instanceof Map)) {
                    throw invalid("'profiles' must be a YAML object in " + source.toAbsolutePath());
                }
                ((Map<?, ?>) rawProfiles).forEach((id, value) -> {
                    if (!(value instanceof Map)) {
                        throw invalid("Profile '" + id + "' must be a YAML object in " + source.toAbsolutePath());
                    }
                    profiles.put(String.valueOf(id), copyMap((Map<?, ?>) value));
                });
            }
            return new ResolvedConfig(
                    defaultProfile,
                    copyMap(document),
                    Collections.unmodifiableMap(profiles),
                    environment,
                    source
            );
        }

        String value(String profileId, String key) {
            if (isBlank(profileId)) {
                profileId = defaultProfile;
            }
            Map<String, Object> profile = profiles.get(profileId);
            String path = KEY_PATHS.getOrDefault(key, key.toLowerCase(Locale.ROOT).replace('_', '.'));
            Object value = profile == null ? null : nestedValue(profile, path);
            String sourcePath = value == null ? null : "profiles." + profileId + "." + path;
            if ("PEPENIUM_WEB_CAPABILITIES".equals(key)
                    || "PEPENIUM_APPIUM_CAPABILITIES".equals(key)) {
                return null;
            }
            if (value == null) {
                value = settingValue(profile, key);
                if (value != null) {
                    sourcePath = "profiles." + profileId + ".settings." + key;
                }
            }
            if (value == null) {
                value = nestedValue(global, path);
                if (value != null) {
                    sourcePath = path;
                }
            }
            if (value == null) {
                value = settingValue(global, key);
                if (value != null) {
                    sourcePath = "settings." + key;
                }
            }
            return render(value, sourcePath);
        }

        private static Object settingValue(Map<String, Object> source, String key) {
            if (source == null) {
                return null;
            }
            return mapValue(source.get("settings")).get(key);
        }

        Map<String, Object> capabilities(String profileId) {
            if (isBlank(profileId)) {
                profileId = defaultProfile;
            }
            Map<String, Object> merged = new LinkedHashMap<>();
            mergeMaps(merged, mapValue(global.get("capabilities")));
            Map<String, Object> profile = profiles.get(profileId);
            if (profile != null) {
                mergeMaps(merged, mapValue(profile.get("capabilities")));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> resolved = (Map<String, Object>) resolveNode(
                    merged,
                    isBlank(profileId) ? "capabilities" : "profiles." + profileId + ".capabilities"
            );
            return resolved;
        }

        Map<String, Object> settings(String profileId) {
            if (isBlank(profileId)) {
                profileId = defaultProfile;
            }
            Map<String, Object> merged = new LinkedHashMap<>();
            mergeMaps(merged, mapValue(global.get("settings")));
            Map<String, Object> profile = profiles.get(profileId);
            if (profile != null) {
                mergeMaps(merged, mapValue(profile.get("settings")));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> resolved = (Map<String, Object>) resolveNode(
                    merged,
                    isBlank(profileId) ? "settings" : "profiles." + profileId + ".settings"
            );
            return resolved;
        }

        void validateProfile(String profileId) {
            Map<String, Object> profile = profiles.get(profileId);
            if (profile == null) {
                return;
            }
            if (profileId.startsWith("aws-") || profileId.startsWith("browserstack-")) {
                for (String providerOwnedSection : List.of("serverUrl", "device", "app", "browser")) {
                    if (profile.containsKey(providerOwnedSection)) {
                        throw invalid("'profiles." + profileId + "." + providerOwnedSection
                                + "' is provider-owned and cannot be configured in pepenium.yml. "
                                + providerHint(profileId));
                    }
                }
            }
            if (profileId.startsWith("browserstack-")) {
                rejectBrowserStackOwnedCapabilities(mapValue(global.get("capabilities")), "capabilities");
                rejectBrowserStackOwnedCapabilities(
                        mapValue(profile.get("capabilities")),
                        "profiles." + profileId + ".capabilities"
                );
            }
        }

        void validateResolvedProfile(String profileId) {
            validateProfile(profileId);
            for (String key : KEY_PATHS.keySet()) {
                validateResolvedValue(key, value(profileId, key), profileId);
            }
            capabilities(profileId);
            settings(profileId);
        }

        private void validateResolvedValue(String key, String value, String profileId) {
            if (value == null) {
                return;
            }
            String path = resolvedPath(profileId, KEY_PATHS.get(key));
            switch (key) {
                case "APPIUM_URL":
                case "PEPENIUM_BASE_URL":
                    validateResolvedHttpUrl(value, path, source);
                    break;
                case "PEPENIUM_WEB_HEADLESS":
                case "PEPENIUM_WEB_ACCEPT_INSECURE_CERTS":
                case "PEPENIUM_DETAIL_LOGGING":
                    validateResolvedBoolean(value, path, source);
                    break;
                case "PEPENIUM_STEP_TRACKER_LIMIT":
                    validateResolvedPositiveInteger(value, path, source);
                    break;
                case "PEPENIUM_ACTION_TIMEOUT_SECONDS":
                case "PEPENIUM_ACTION_LONG_TIMEOUT_SECONDS":
                case "PEPENIUM_ASSERTION_TIMEOUT_SECONDS":
                    validateDuration(value, path, source);
                    break;
                case "PEPENIUM_WEB_PAGE_LOAD_STRATEGY":
                    if (!Set.of("normal", "eager", "none").contains(value)) {
                        throw invalid("'" + path + "' must resolve to normal, eager or none in "
                                + source.toAbsolutePath());
                    }
                    break;
                default:
                    break;
            }
        }

        private String resolvedPath(String profileId, String path) {
            return profiles.containsKey(profileId) && nestedValue(profiles.get(profileId), path) != null
                    ? "profiles." + profileId + "." + path
                    : path;
        }

        String defaultProfile() {
            return defaultProfile;
        }

        private String render(Object value, String path) {
            if (value == null) {
                return null;
            }
            if (value instanceof List) {
                List<String> values = new ArrayList<>();
                for (Object item : (List<?>) value) {
                    values.add(resolvePlaceholders(String.valueOf(item), environment, source, path));
                }
                return String.join(";", values);
            }
            if (value instanceof Map) {
                List<String> values = new ArrayList<>();
                ((Map<?, ?>) value).forEach((key, item) -> values.add(
                        key + "=" + resolvePlaceholders(String.valueOf(item), environment, source, path)
                ));
                return String.join(";", values);
            }
            return resolvePlaceholders(String.valueOf(value), environment, source, path);
        }

        private Object resolveNode(Object value, String path) {
            if (value instanceof Map) {
                Map<String, Object> resolved = new LinkedHashMap<>();
                ((Map<?, ?>) value).forEach((key, item) -> resolved.put(
                        String.valueOf(key),
                        resolveNode(item, path + "." + key)
                ));
                return Collections.unmodifiableMap(resolved);
            }
            if (value instanceof List) {
                List<Object> resolved = new ArrayList<>();
                List<?> values = (List<?>) value;
                for (int index = 0; index < values.size(); index++) {
                    resolved.add(resolveNode(values.get(index), path + "[" + index + "]"));
                }
                return Collections.unmodifiableList(resolved);
            }
            if (value instanceof String) {
                return resolvePlaceholders((String) value, environment, source, path);
            }
            return value;
        }

        private static Map<String, Object> copyMap(Map<?, ?> source) {
            Map<String, Object> result = new LinkedHashMap<>();
            source.forEach((key, value) -> result.put(String.valueOf(key), value));
            return Collections.unmodifiableMap(result);
        }

        private static Map<String, Object> mapValue(Object value) {
            if (value == null) {
                return Collections.emptyMap();
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return map;
        }

        private static void mergeMaps(Map<String, Object> target, Map<String, Object> source) {
            source.forEach((key, value) -> {
                Object existing = target.get(key);
                if (existing instanceof Map && value instanceof Map) {
                    Map<String, Object> nested = new LinkedHashMap<>();
                    nested.putAll(mapValue(existing));
                    mergeMaps(nested, mapValue(value));
                    target.put(key, nested);
                } else {
                    target.put(key, value);
                }
            });
        }

        private static Object nestedValue(Map<String, Object> source, String path) {
            Object current = source;
            for (String part : path.split("\\.")) {
                if (!(current instanceof Map)) {
                    return null;
                }
                current = ((Map<?, ?>) current).get(part);
            }
            return current;
        }

        private static String scalar(Object value,
                                     String path,
                                     Function<String, String> environment,
                                     Path source) {
            if (value == null) {
                return null;
            }
            if (value instanceof Map || value instanceof List) {
                throw invalid("'" + path + "' must be a scalar value in " + source.toAbsolutePath());
            }
            return resolvePlaceholders(String.valueOf(value), environment, source, path);
        }

        private static String resolvePlaceholders(String value,
                                                  Function<String, String> environment,
                                                  Path source,
                                                  String path) {
            Matcher matcher = PLACEHOLDER.matcher(value);
            StringBuffer resolved = new StringBuffer();
            while (matcher.find()) {
                String replacement = environment.apply(matcher.group(1));
                if (isBlank(replacement)) {
                    throw invalid("Missing environment variable '" + matcher.group(1)
                            + "' referenced by '" + path + "' in " + source.toAbsolutePath());
                }
                matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(resolved);
            return resolved.toString().trim();
        }

        private static void validateDocument(Map<?, ?> document, Path source) {
            validateKeys(document, ROOT_KEYS, "root", source);
            validateSchemaVersion(document.get("schemaVersion"), source);
            validateScalar(document, "defaultProfile", "defaultProfile", source);
            validateHttpUrl(document, "baseUrl", "baseUrl", source);
            validateSection(document, "credentials", Set.of("username", "password"), "credentials", source);
            validateSection(document, "reporting", Set.of("directory", "screenshotPath"), "reporting", source);
            validateSection(document, "logging", Set.of("detailed", "stepLimit"), "logging", source);
            validateSection(document, "timeouts", Set.of(
                    "action", "longAction", "assertion", "sessionConnect", "sessionCommand"
            ), "timeouts", source);
            validateCommonSections(document, "", source);
            validateCapabilities(document.get("capabilities"), "capabilities", source);
            validateSettings(document.get("settings"), "settings", source);

            Object rawProfiles = document.get("profiles");
            if (rawProfiles == null) {
                return;
            }
            Map<?, ?> profileMap = requireMap(rawProfiles, "profiles", source);
            profileMap.forEach((profileId, rawProfile) -> {
                if (!(profileId instanceof String) || ((String) profileId).isBlank()) {
                    throw invalid("Profile ids must be non-blank strings in " + source.toAbsolutePath());
                }
                String path = "profiles." + profileId;
                Map<?, ?> profile = requireMap(rawProfile, path, source);
                validateKeys(profile, PROFILE_KEYS, path, source);
                validateHttpUrl(profile, "serverUrl", path + ".serverUrl", source);
                validateHttpUrl(profile, "baseUrl", path + ".baseUrl", source);
                validateSection(profile, "device", Set.of("udid", "name", "platformVersion"),
                        path + ".device", source);
                validateSection(profile, "app", Set.of("path", "package", "activity", "bundleId"),
                        path + ".app", source);
                validateSection(profile, "browser", Set.of(
                        "headless", "acceptInsecureCerts", "pageLoadStrategy", "version", "binary", "arguments"
                ), path + ".browser", source);
                validateSection(profile, "credentials", Set.of("username", "password"), path + ".credentials", source);
                validateSection(profile, "reporting", Set.of("directory", "screenshotPath"), path + ".reporting", source);
                validateSection(profile, "logging", Set.of("detailed", "stepLimit"), path + ".logging", source);
                validateSection(profile, "timeouts", Set.of(
                        "action", "longAction", "assertion", "sessionConnect", "sessionCommand"
                ), path + ".timeouts", source);
                validateCommonSections(profile, path + ".", source);
                validateScalarSection(profile, "device", path + ".device", source);
                validateScalarSection(profile, "app", path + ".app", source);
                validateBrowserSection(profile, path + ".browser", source);
                validateCapabilities(profile.get("capabilities"), path + ".capabilities", source);
                validateSettings(profile.get("settings"), path + ".settings", source);
            });
        }

        private static void validateSchemaVersion(Object value, Path source) {
            if (value == null) {
                return;
            }
            if (!(value instanceof Number) || ((Number) value).doubleValue() != 1d) {
                throw invalid("'schemaVersion' must be the integer 1 in " + source.toAbsolutePath());
            }
        }

        private static void validateCommonSections(Map<?, ?> parent, String prefix, Path source) {
            validateScalarSection(parent, "credentials", prefix + "credentials", source);
            validateScalarSection(parent, "reporting", prefix + "reporting", source);
            Object loggingValue = parent.get("logging");
            if (loggingValue instanceof Map) {
                Map<?, ?> logging = (Map<?, ?>) loggingValue;
                validateBoolean(logging.get("detailed"), prefix + "logging.detailed", source);
                validatePositiveInteger(logging.get("stepLimit"), prefix + "logging.stepLimit", source);
            }
            Object timeoutValue = parent.get("timeouts");
            if (timeoutValue instanceof Map) {
                ((Map<?, ?>) timeoutValue).forEach((key, value) ->
                        validateDuration(value, prefix + "timeouts." + key, source));
            }
        }

        private static void validateBrowserSection(Map<?, ?> parent, String path, Path source) {
            Object value = parent.get("browser");
            if (!(value instanceof Map)) {
                return;
            }
            Map<?, ?> browser = (Map<?, ?>) value;
            validateBoolean(browser.get("headless"), path + ".headless", source);
            validateBoolean(browser.get("acceptInsecureCerts"), path + ".acceptInsecureCerts", source);
            Object strategy = browser.get("pageLoadStrategy");
            if (strategy != null && !isPlaceholder(strategy)
                    && !Set.of("normal", "eager", "none").contains(String.valueOf(strategy))) {
                throw invalid("'" + path + ".pageLoadStrategy' must be normal, eager or none in "
                        + source.toAbsolutePath());
            }
            Object arguments = browser.get("arguments");
            if (arguments != null) {
                if (!(arguments instanceof List)) {
                    throw invalid("'" + path + ".arguments' must be a YAML list in " + source.toAbsolutePath());
                }
                List<?> argumentList = (List<?>) arguments;
                for (int index = 0; index < argumentList.size(); index++) {
                    validateScalarValue(argumentList.get(index), path + ".arguments[" + index + "]", source);
                }
            }
            validateScalar(browser, "version", path + ".version", source);
            validateScalar(browser, "binary", path + ".binary", source);
        }

        private static void validateScalarSection(Map<?, ?> parent, String key, String path, Path source) {
            Object value = parent.get(key);
            if (!(value instanceof Map)) {
                return;
            }
            ((Map<?, ?>) value).forEach((nestedKey, nestedValue) ->
                    validateScalarValue(nestedValue, path + "." + nestedKey, source));
        }

        private static void validateScalar(Map<?, ?> parent, String key, String path, Path source) {
            if (parent.containsKey(key)) {
                validateScalarValue(parent.get(key), path, source);
            }
        }

        private static void validateScalarValue(Object value, String path, Path source) {
            if (value instanceof Map || value instanceof List || value == null) {
                throw invalid("'" + path + "' must be a non-null scalar value in " + source.toAbsolutePath());
            }
            if (value instanceof String && ((String) value).isBlank()) {
                throw invalid("'" + path + "' must not be blank in " + source.toAbsolutePath());
            }
        }

        private static void validateBoolean(Object value, String path, Path source) {
            if (value == null || value instanceof Boolean || isPlaceholder(value)) {
                return;
            }
            throw invalid("'" + path + "' must be true or false in " + source.toAbsolutePath());
        }

        private static void validatePositiveInteger(Object value, String path, Path source) {
            if (value == null || isPlaceholder(value)) {
                return;
            }
            if (!(value instanceof Number) || ((Number) value).longValue() <= 0
                    || ((Number) value).doubleValue() != ((Number) value).longValue()) {
                throw invalid("'" + path + "' must be a positive integer in " + source.toAbsolutePath());
            }
        }

        private static void validateDuration(Object value, String path, Path source) {
            if (value == null || isPlaceholder(value)) {
                return;
            }
            String duration = String.valueOf(value).trim();
            boolean valid = duration.matches("(?i)[1-9][0-9]*(ms|s|m)?");
            if (!valid && duration.toUpperCase(Locale.ROOT).startsWith("PT")) {
                try {
                    Duration parsed = Duration.parse(duration.toUpperCase(Locale.ROOT));
                    valid = !parsed.isZero() && !parsed.isNegative();
                } catch (DateTimeParseException ignored) {
                    valid = false;
                }
            }
            if (!valid) {
                throw invalid("'" + path + "' must be a positive duration such as 750ms, 10s, 2m or PT10S in "
                        + source.toAbsolutePath());
            }
        }

        private static void validateHttpUrl(Map<?, ?> parent, String key, String path, Path source) {
            Object value = parent.get(key);
            if (value == null || isPlaceholder(value)) {
                return;
            }
            validateScalarValue(value, path, source);
            try {
                URI uri = new URI(String.valueOf(value));
                if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                        || uri.getHost() == null) {
                    throw new URISyntaxException(String.valueOf(value), "HTTP(S) URL with host required");
                }
            } catch (URISyntaxException error) {
                throw invalid("'" + path + "' must be a valid HTTP(S) URL in " + source.toAbsolutePath(), error);
            }
        }

        private static void validateResolvedHttpUrl(String value, String path, Path source) {
            try {
                URI uri = new URI(value);
                if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                        || uri.getHost() == null) {
                    throw new URISyntaxException(value, "HTTP(S) URL with host required");
                }
            } catch (URISyntaxException error) {
                throw invalid("'" + path + "' must resolve to a valid HTTP(S) URL in "
                        + source.toAbsolutePath(), error);
            }
        }

        private static void validateResolvedBoolean(String value, String path, Path source) {
            if (!("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))) {
                throw invalid("'" + path + "' must resolve to true or false in " + source.toAbsolutePath());
            }
        }

        private static void validateResolvedPositiveInteger(String value, String path, Path source) {
            try {
                if (Long.parseLong(value) <= 0) {
                    throw new NumberFormatException("not positive");
                }
            } catch (NumberFormatException error) {
                throw invalid("'" + path + "' must resolve to a positive integer in "
                        + source.toAbsolutePath(), error);
            }
        }

        private static boolean isPlaceholder(Object value) {
            return value instanceof String && PLACEHOLDER.matcher((String) value).find();
        }

        private static void rejectBrowserStackOwnedCapabilities(Map<String, Object> capabilities, String path) {
            for (String key : capabilities.keySet()) {
                if ("bstack:options".equals(key) || key.startsWith("browserstack.")) {
                    throw invalid("'" + path + "." + key
                            + "' belongs in the existing BrowserStack YAML, not pepenium.yml.");
                }
            }
        }

        private static String providerHint(String profileId) {
            return profileId.startsWith("browserstack-")
                    ? "Keep BrowserStack platform and credential settings in its existing YAML."
                    : "AWS device and Appium settings are supplied by Device Farm environment variables.";
        }

        private static void validateSection(Map<?, ?> parent,
                                            String key,
                                            Set<String> allowed,
                                            String path,
                                            Path source) {
            Object value = parent.get(key);
            if (value != null) {
                validateKeys(requireMap(value, path, source), allowed, path, source);
            }
        }

        private static void validateCapabilities(Object value, String path, Path source) {
            if (value != null) {
                validateCapabilityNode(requireMap(value, path, source), path, source);
            }
        }

        private static void validateSettings(Object value, String path, Path source) {
            if (value == null) {
                return;
            }
            Map<?, ?> settings = requireMap(value, path, source);
            settings.forEach((key, setting) -> {
                if (!(key instanceof String) || ((String) key).isBlank()) {
                    throw invalid("Setting keys must be non-blank strings at '" + path
                            + "' in " + source.toAbsolutePath());
                }
                if (KEY_PATHS.containsKey(key)) {
                    throw invalid("'" + path + "." + key + "' is a built-in setting. Use '"
                            + KEY_PATHS.get(key) + "' instead in " + source.toAbsolutePath());
                }
                validateCapabilityNode(setting, path + "." + key, source);
            });
        }

        private static void validateCapabilityNode(Object value, String path, Path source) {
            if (value instanceof Map) {
                ((Map<?, ?>) value).forEach((key, nested) -> {
                    if (!(key instanceof String) || ((String) key).isBlank()) {
                        throw invalid("Capability keys must be non-blank strings at '" + path
                                + "' in " + source.toAbsolutePath());
                    }
                    validateCapabilityNode(nested, path + "." + key, source);
                });
                return;
            }
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                for (int index = 0; index < list.size(); index++) {
                    validateCapabilityNode(list.get(index), path + "[" + index + "]", source);
                }
                return;
            }
            if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
                return;
            }
            throw invalid("Unsupported capability value at '" + path + "' in " + source.toAbsolutePath());
        }

        private static Map<?, ?> requireMap(Object value, String path, Path source) {
            if (!(value instanceof Map)) {
                throw invalid("'" + path + "' must be a YAML object in " + source.toAbsolutePath());
            }
            return (Map<?, ?>) value;
        }

        private static void validateKeys(Map<?, ?> values, Set<String> allowed, String path, Path source) {
            for (Object key : values.keySet()) {
                String name = String.valueOf(key);
                if (!allowed.contains(name)) {
                    throw invalid("Unknown key '" + path + "." + name + "' in " + source.toAbsolutePath());
                }
            }
        }
    }
}
