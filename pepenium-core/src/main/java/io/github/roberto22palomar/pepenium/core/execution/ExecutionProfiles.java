package io.github.roberto22palomar.pepenium.core.execution;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    public static String availableProfileIdsMultiline() {
        return PROFILES.keySet().stream()
                .map(profileId -> "- " + profileId)
                .collect(Collectors.joining(System.lineSeparator()));
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
                            + ". Description: " + profile.getDescription()
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

    public static String compatibleProfileIdsMultiline(TestTarget target) {
        return list().stream()
                .filter(profile -> profile.getTarget() == target)
                .map(ExecutionProfile::getId)
                .map(profileId -> "- " + profileId)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public static String suggestedProfileIdsMultiline(String requestedProfileId, TestTarget target) {
        if (isBlank(requestedProfileId)) {
            return "";
        }
        String requested = requestedProfileId.trim();
        int maximumDistance = Math.max(2, requested.length() / 3);
        return list().stream()
                .filter(profile -> target == null || profile.getTarget() == target)
                .map(ExecutionProfile::getId)
                .map(profileId -> new ProfileSuggestion(profileId, levenshteinDistance(requested, profileId)))
                .filter(suggestion -> suggestion.distance <= maximumDistance)
                .sorted(Comparator
                        .comparingInt(ProfileSuggestion::distance)
                        .thenComparing(ProfileSuggestion::profileId))
                .limit(3)
                .map(ProfileSuggestion::profileId)
                .map(profileId -> "- " + profileId)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static Map<String, ExecutionProfile> loadProfiles() {
        try (InputStream input = ExecutionProfiles.class.getClassLoader().getResourceAsStream(PROFILES_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing required resource: " + PROFILES_RESOURCE);
            }

            ProfilesFile yamlFile = new Yaml().loadAs(input, ProfilesFile.class);
            return loadProfiles(yamlFile);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load execution profiles from " + PROFILES_RESOURCE, e);
        }
    }

    static Map<String, ExecutionProfile> loadProfiles(ProfilesFile yamlFile) {
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
    }

    static ExecutionProfile toExecutionProfile(ProfileDefinition definition) {
        if (definition == null) {
            throw new IllegalStateException("Found null execution profile definition in " + PROFILES_RESOURCE);
        }
        if (isBlank(definition.getId())) {
            throw new IllegalStateException("Execution profile id is required in " + PROFILES_RESOURCE);
        }
        if (definition.getTarget() == null) {
            throw new IllegalStateException("Execution profile target is required for '" + definition.getId() + "'");
        }
        if (isBlank(definition.getDescription())) {
            throw new IllegalStateException("Execution profile description is required for '" + definition.getId() + "'");
        }
        if (definition.getConfigKey() == null) {
            throw new IllegalStateException("Execution profile configKey is required for '" + definition.getId() + "'");
        }

        return new ExecutionProfile(
                definition.getId().trim(),
                definition.getTarget(),
                definition.getDescription().trim(),
                buildConfigSupplier(definition.getConfigKey())
        );
    }

    private static Supplier<DriverConfig> buildConfigSupplier(BuiltInDriverConfigKey configKey) {
        return configKey.supplier();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static int levenshteinDistance(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + cost
                );
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[right.length()];
    }

    private static final class ProfileSuggestion {
        private final String profileId;
        private final int distance;

        private ProfileSuggestion(String profileId, int distance) {
            this.profileId = profileId;
            this.distance = distance;
        }

        private String profileId() {
            return profileId;
        }

        private int distance() {
            return distance;
        }
    }

    public static class ProfilesFile {
        private List<ProfileDefinition> profiles;

        public List<ProfileDefinition> getProfiles() {
            return profiles == null ? null : Collections.unmodifiableList(profiles);
        }

        public void setProfiles(List<ProfileDefinition> profiles) {
            this.profiles = profiles == null ? null : new ArrayList<>(profiles);
        }
    }

    public static class ProfileDefinition {
        private String id;
        private TestTarget target;
        private String description;
        private BuiltInDriverConfigKey configKey;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public TestTarget getTarget() {
            return target;
        }

        public void setTarget(TestTarget target) {
            this.target = target;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BuiltInDriverConfigKey getConfigKey() {
            return configKey;
        }

        public void setConfigKey(BuiltInDriverConfigKey configKey) {
            this.configKey = configKey;
        }
    }
}
