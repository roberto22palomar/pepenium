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
        System.setProperty("pepenium.profile", "browserstack-mac-web");

        ExecutionProfile profile = resolver.resolve(TestTarget.WEB_DESKTOP, "local-web");

        assertEquals("browserstack-mac-web", profile.getId());
    }

    @Test
    void rejectsIncompatibleProfileForTarget() {
        System.setProperty("pepenium.profile", "local-web");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(TestTarget.ANDROID_NATIVE, null)
        );

        assertTrue(error.getMessage().contains("Compatible profiles for ANDROID_NATIVE"));
        assertTrue(error.getMessage().contains("Description:"));
    }

    @Test
    void failsWhenTargetHasNoDefaultAndCallerProvidesNone() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(TestTarget.IOS_NATIVE, null)
        );

        assertTrue(error.getMessage().contains("No execution profile was provided for target IOS_NATIVE"));
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
    }
}
