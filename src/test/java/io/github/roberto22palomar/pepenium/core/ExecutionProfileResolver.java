package io.github.roberto22palomar.pepenium.core;

public class ExecutionProfileResolver {

    public ExecutionProfile resolve(TestTarget target, String defaultProfileId) {
        String profileId = readOverride();
        if (profileId == null || profileId.isBlank()) {
            profileId = defaultProfileId != null ? defaultProfileId : target.getDefaultProfileId();
        }

        if (profileId == null || profileId.isBlank()) {
            throw new IllegalStateException(
                    "No execution profile was provided for target " + target
                            + ". Use -Dpepenium.profile=<profileId> or override getDefaultProfileId(). "
                            + "Compatible profiles: " + ExecutionProfiles.compatibleProfileIds(target)
            );
        }

        ExecutionProfile profile = ExecutionProfiles.get(profileId);
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
}
