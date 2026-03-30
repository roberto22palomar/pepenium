package io.github.roberto22palomar.pepenium.core.execution;

public class ExecutionProfileResolver {

    public ExecutionProfile resolve(TestTarget target, String defaultProfileId) {
        String profileId = readOverride();
        String overrideSource = profileId != null && !profileId.isBlank() ? overrideSource() : null;
        if (profileId == null || profileId.isBlank()) {
            profileId = defaultProfileId != null ? defaultProfileId : target.getDefaultProfileId();
        }

        if (profileId == null || profileId.isBlank()) {
            throw new IllegalStateException(
                    "No execution profile was provided for target " + target
                            + ". Use -Dpepenium.profile=<profileId> or override getDefaultProfileId()."
                            + System.lineSeparator() + System.lineSeparator()
                            + "Compatible profiles for " + target + ":"
                            + System.lineSeparator()
                            + ExecutionProfiles.compatibleProfileIdsMultiline(target)
            );
        }

        ExecutionProfile profile;
        try {
            profile = ExecutionProfiles.get(profileId);
        } catch (IllegalArgumentException e) {
            String sourceMessage = overrideSource != null
                    ? " via " + overrideSource
                    : "";
            throw new IllegalStateException(
                    "Unknown execution profile '" + profileId + "'" + sourceMessage
                            + " for target " + target
                            + "."
                            + System.lineSeparator() + System.lineSeparator()
                            + "Compatible profiles for " + target + ":"
                            + System.lineSeparator()
                            + ExecutionProfiles.compatibleProfileIdsMultiline(target)
                            + System.lineSeparator() + System.lineSeparator()
                            + "All available profiles:"
                            + System.lineSeparator()
                            + ExecutionProfiles.availableProfileIdsMultiline(),
                    e
            );
        }
        ExecutionProfiles.validateCompatibility(profile, target);
        return profile;
    }

    private String readOverride() {
        String systemProperty = System.getProperty("pepenium.profile");
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String envVar = System.getenv("PEPENIUM_PROFILE");
        if (envVar != null && !envVar.isBlank()) {
            return envVar.trim();
        }

        return null;
    }

    private String overrideSource() {
        String systemProperty = System.getProperty("pepenium.profile");
        if (systemProperty != null && !systemProperty.isBlank()) {
            return "-Dpepenium.profile";
        }

        String envVar = System.getenv("PEPENIUM_PROFILE");
        if (envVar != null && !envVar.isBlank()) {
            return "PEPENIUM_PROFILE";
        }

        return null;
    }
}
