package io.github.roberto22palomar.pepenium.core;

import lombok.Getter;
import lombok.Setter;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ExecutionProfiles {

    private static final String PROFILES_RESOURCE = "execution-profiles.yml";
    private static final Map<String, ExecutionProfile> PROFILES = loadProfiles();

    private ExecutionProfiles() {
    }

    public static ExecutionProfile get(String profileId) {
        ExecutionProfile profile = PROFILES.get(profileId);
        if (profile == null) {
            throw new IllegalArgumentException(
                    "Unknown Pepenium execution profile: " + profileId
                            + ". Available profiles: " + availableProfileIds()
            );
        }
        return profile;
    }

    public static boolean exists(String profileId) {
        return profileId != null && PROFILES.containsKey(profileId);
    }

    public static List<ExecutionProfile> list() {
        return Collections.unmodifiableList(new ArrayList<>(PROFILES.values()));
    }

    public static String availableProfileIds() {
        return PROFILES.keySet().stream().collect(Collectors.joining(", "));
    }

    public static String describeAll() {
        return list().stream()
                .map(profile -> String.format("- %s [%s] %s",
                        profile.getId(),
                        profile.getTarget(),
                        profile.getDescription()))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public static void validateCompatibility(ExecutionProfile profile, TestTarget target) {
        if (profile.getTarget() != target) {
            throw new IllegalStateException(
                    "Execution profile '" + profile.getId() + "' targets " + profile.getTarget()
                            + " but the test targets " + target
                            + ". Compatible profiles for " + target + ": " + compatibleProfileIds(target)
            );
        }
    }

    public static String compatibleProfileIds(TestTarget target) {
        return list().stream()
                .filter(profile -> profile.getTarget() == target)
                .map(ExecutionProfile::getId)
                .collect(Collectors.joining(", "));
    }

    private static Map<String, ExecutionProfile> loadProfiles() {
        try (InputStream input = ExecutionProfiles.class.getClassLoader().getResourceAsStream(PROFILES_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing required resource: " + PROFILES_RESOURCE);
            }

            ProfilesFile yamlFile = new Yaml().loadAs(input, ProfilesFile.class);
            if (yamlFile == null || yamlFile.getProfiles() == null || yamlFile.getProfiles().isEmpty()) {
                throw new IllegalStateException("No execution profiles found in " + PROFILES_RESOURCE);
            }

            Map<String, ExecutionProfile> profiles = new LinkedHashMap<>();
            for (ProfileDefinition definition : yamlFile.getProfiles()) {
                ExecutionProfile profile = toExecutionProfile(definition);
                if (profiles.putIfAbsent(profile.getId(), profile) != null) {
                    throw new IllegalStateException("Duplicate execution profile id '" + profile.getId()
                            + "' in " + PROFILES_RESOURCE);
                }
            }
            return profiles;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load execution profiles from " + PROFILES_RESOURCE, e);
        }
    }

    private static ExecutionProfile toExecutionProfile(ProfileDefinition definition) {
        if (definition == null) {
            throw new IllegalStateException("Found null execution profile definition in " + PROFILES_RESOURCE);
        }
        if (isBlank(definition.getId())) {
            throw new IllegalStateException("Execution profile id is required in " + PROFILES_RESOURCE);
        }
        if (isBlank(definition.getTarget())) {
            throw new IllegalStateException("Execution profile target is required for '" + definition.getId() + "'");
        }
        if (isBlank(definition.getDescription())) {
            throw new IllegalStateException("Execution profile description is required for '" + definition.getId() + "'");
        }
        if (isBlank(definition.getConfigClass())) {
            throw new IllegalStateException("Execution profile configClass is required for '" + definition.getId() + "'");
        }

        TestTarget target;
        try {
            target = TestTarget.valueOf(definition.getTarget().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unknown target '" + definition.getTarget()
                    + "' for execution profile '" + definition.getId() + "'", e);
        }

        return new ExecutionProfile(
                definition.getId().trim(),
                target,
                definition.getDescription().trim(),
                buildConfigSupplier(definition.getConfigClass().trim(), definition.getId().trim())
        );
    }

    @SuppressWarnings("unchecked")
    private static Supplier<DriverConfig> buildConfigSupplier(String configClassName, String profileId) {
        try {
            Class<?> rawClass = Class.forName(configClassName);
            if (!DriverConfig.class.isAssignableFrom(rawClass)) {
                throw new IllegalStateException("Config class '" + configClassName
                        + "' for execution profile '" + profileId + "' does not extend DriverConfig");
            }

            Class<? extends DriverConfig> configClass = (Class<? extends DriverConfig>) rawClass;
            return () -> {
                try {
                    return configClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to instantiate config class '" + configClassName
                            + "' for execution profile '" + profileId + "'", e);
                }
            };
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Config class '" + configClassName
                    + "' for execution profile '" + profileId + "' was not found", e);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Getter
    @Setter
    public static class ProfilesFile {
        private List<ProfileDefinition> profiles;
    }

    @Getter
    @Setter
    public static class ProfileDefinition {
        private String id;
        private String target;
        private String description;
        private String configClass;
    }
}
