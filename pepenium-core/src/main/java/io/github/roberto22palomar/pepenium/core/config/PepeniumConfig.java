package io.github.roberto22palomar.pepenium.core.config;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
            "defaultProfile", "baseUrl", "credentials", "reporting", "logging", "timeouts",
            "capabilities", "profiles"
    );
    private static final Set<String> PROFILE_KEYS = Set.of(
            "serverUrl", "device", "app", "browser", "baseUrl", "credentials", "reporting",
            "logging", "timeouts", "capabilities"
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

    static ResolvedConfig load(Path path, boolean explicit, Function<String, String> environment) {
        if (!Files.exists(path)) {
            if (explicit) {
                throw invalid("Configuration file does not exist: " + path.toAbsolutePath());
            }
            return ResolvedConfig.empty(environment);
        }
        try (InputStream input = Files.newInputStream(path)) {
            Object document = new Yaml().load(input);
            if (document == null) {
                return ResolvedConfig.empty(environment);
            }
            if (!(document instanceof Map)) {
                throw invalid("Configuration root must be a YAML object in " + path.toAbsolutePath());
            }
            return ResolvedConfig.from((Map<?, ?>) document, environment, path);
        } catch (IOException error) {
            throw invalid("Could not read configuration file " + path.toAbsolutePath(), error);
        }
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
        paths.put("APP_PATH", "app.path");
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
            boolean fromProfile = value != null;
            if ("PEPENIUM_WEB_CAPABILITIES".equals(key)
                    || "PEPENIUM_APPIUM_CAPABILITIES".equals(key)) {
                return null;
            }
            if (value == null) {
                value = nestedValue(global, path);
            }
            String sourcePath = fromProfile
                    ? "profiles." + profileId + "." + path
                    : path;
            return render(value, sourcePath);
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
            validateSection(document, "credentials", Set.of("username", "password"), "credentials", source);
            validateSection(document, "reporting", Set.of("directory", "screenshotPath"), "reporting", source);
            validateSection(document, "logging", Set.of("detailed", "stepLimit"), "logging", source);
            validateSection(document, "timeouts", Set.of("action", "longAction", "assertion"), "timeouts", source);
            validateCapabilities(document.get("capabilities"), "capabilities", source);

            Object rawProfiles = document.get("profiles");
            if (rawProfiles == null) {
                return;
            }
            Map<?, ?> profileMap = requireMap(rawProfiles, "profiles", source);
            profileMap.forEach((profileId, rawProfile) -> {
                String path = "profiles." + profileId;
                Map<?, ?> profile = requireMap(rawProfile, path, source);
                validateKeys(profile, PROFILE_KEYS, path, source);
                validateSection(profile, "device", Set.of("udid", "name"), path + ".device", source);
                validateSection(profile, "app", Set.of("path", "package", "activity"), path + ".app", source);
                validateSection(profile, "browser", Set.of(
                        "headless", "acceptInsecureCerts", "pageLoadStrategy", "version", "binary", "arguments"
                ), path + ".browser", source);
                validateSection(profile, "credentials", Set.of("username", "password"), path + ".credentials", source);
                validateSection(profile, "reporting", Set.of("directory", "screenshotPath"), path + ".reporting", source);
                validateSection(profile, "logging", Set.of("detailed", "stepLimit"), path + ".logging", source);
                validateSection(profile, "timeouts", Set.of("action", "longAction", "assertion"), path + ".timeouts", source);
                validateCapabilities(profile.get("capabilities"), path + ".capabilities", source);
            });
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
