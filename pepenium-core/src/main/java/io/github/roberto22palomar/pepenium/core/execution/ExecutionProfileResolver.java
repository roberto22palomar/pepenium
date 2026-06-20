package io.github.roberto22palomar.pepenium.core.execution;

import io.github.roberto22palomar.pepenium.core.config.PepeniumConfig;

public class ExecutionProfileResolver {

    public ExecutionProfile resolve(TestTarget target, String defaultProfileId) {
        String profileId = readOverride();
        String overrideSource = profileId != null && !profileId.isBlank() ? overrideSource() : null;
        if (profileId == null || profileId.isBlank()) {
            profileId = defaultProfileId;
        }
        if (profileId == null || profileId.isBlank()) {
            profileId = PepeniumConfig.getDefaultProfile();
        }
        if (profileId == null || profileId.isBlank()) {
            profileId = target.getDefaultProfileId();
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
            String suggestions = ExecutionProfiles.suggestedProfileIdsMultiline(profileId, target);
            String suggestionMessage = suggestions.isBlank()
                    ? ""
                    : System.lineSeparator() + System.lineSeparator()
                            + "Did you mean:"
                            + System.lineSeparator()
                            + suggestions;
            throw new IllegalStateException(
                    "Unknown execution profile '" + profileId + "'" + sourceMessage
                            + " for target " + target
                            + "."
                            + suggestionMessage
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
        PepeniumConfig.activateProfile(profile.getId());
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
