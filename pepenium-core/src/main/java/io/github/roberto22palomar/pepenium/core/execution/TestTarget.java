package io.github.roberto22palomar.pepenium.core.execution;

/**
 * Stable functional targets that test classes can declare through {@code BaseTest}.
 */
public enum TestTarget {
    ANDROID_NATIVE("local-android"),
    ANDROID_WEB("local-android-web"),
    IOS_NATIVE(null),
    IOS_WEB(null),
    WEB_DESKTOP("local-web");

    private final String defaultProfileId;

    TestTarget(String defaultProfileId) {
        this.defaultProfileId = defaultProfileId;
    }

    public String getDefaultProfileId() {
        return defaultProfileId;
    }
}
