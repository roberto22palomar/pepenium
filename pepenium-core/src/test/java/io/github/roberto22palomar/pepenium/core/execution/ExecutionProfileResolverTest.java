package io.github.roberto22palomar.pepenium.core.execution;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionProfileResolverTest {

    private final ExecutionProfileResolver resolver = new ExecutionProfileResolver();

    @AfterEach
    void clearProfileOverride() {
        System.clearProperty("pepenium.profile");
    }

    @Test
    void usesTargetDefaultProfileWhenNoOverrideIsPresent() {
        ExecutionProfile profile = resolver.resolve(TestTarget.WEB_DESKTOP, null);

        assertEquals("local-web", profile.getId());
        assertEquals(TestTarget.WEB_DESKTOP, profile.getTarget());
    }

    @Test
    void systemPropertyOverrideWinsOverTargetDefault() {
        System.setProperty("pepenium.profile", "local-web-firefox");

        ExecutionProfile profile = resolver.resolve(TestTarget.WEB_DESKTOP, "local-web");

        assertEquals("local-web-firefox", profile.getId());
    }

    @Test
    void rejectsIncompatibleProfileForTarget() {
        System.setProperty("pepenium.profile", "local-web");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(TestTarget.ANDROID_NATIVE, null)
        );

        assertTrue(error.getMessage().contains("Compatible profiles for ANDROID_NATIVE"));
        assertTrue(error.getMessage().contains("Profile description:"));
        assertTrue(error.getMessage().contains("Closest compatible profiles:"));
        assertTrue(error.getMessage().contains("- local-android"));
    }

    @Test
    void resolvesLocalIosDefaultsWhenCallerProvidesNone() {
        assertEquals("local-ios", resolver.resolve(TestTarget.IOS_NATIVE, null).getId());
        assertEquals("local-ios-web", resolver.resolve(TestTarget.IOS_WEB, null).getId());
    }

    @Test
    void rejectsUnknownOverrideWithHelpfulSourceMessage() {
        System.setProperty("pepenium.profile", "does-not-exist");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(TestTarget.WEB_DESKTOP, null)
        );

        assertTrue(error.getMessage().contains("Unknown execution profile 'does-not-exist'"));
        assertTrue(error.getMessage().contains("-Dpepenium.profile"));
        assertTrue(error.getMessage().contains("Compatible profiles for WEB_DESKTOP:"));
        assertTrue(error.getMessage().contains("- local-web"));
        assertTrue(error.getMessage().contains("- local-web-firefox"));
        assertTrue(error.getMessage().contains("- local-web-edge"));
        assertTrue(error.getMessage().contains("- browserstack-windows-web"));
        assertTrue(error.getMessage().contains("- browserstack-mac-web"));
        assertTrue(error.getMessage().contains("All available profiles:"));
    }

    @Test
    void suggestsClosestCompatibleProfileWhenOverrideLooksLikeATypo() {
        System.setProperty("pepenium.profile", "local-wbe");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(TestTarget.WEB_DESKTOP, null)
        );

        assertTrue(error.getMessage().contains("Did you mean:"));
        assertTrue(error.getMessage().contains("- local-web"));
    }
}
