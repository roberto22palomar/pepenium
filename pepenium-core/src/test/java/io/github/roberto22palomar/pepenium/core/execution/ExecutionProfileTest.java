package io.github.roberto22palomar.pepenium.core.execution;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionProfileTest {

    @Test
    void normalizesConsumerProvidedMetadata() {
        ExecutionProfile profile = new ExecutionProfile(
                "  team-grid-web  ",
                TestTarget.WEB_DESKTOP,
                "  Team Grid  ",
                () -> () -> null
        );

        assertEquals("team-grid-web", profile.getId());
        assertEquals("Team Grid", profile.getDescription());
    }

    @Test
    void rejectsNullConfigsFromLazySuppliers() {
        ExecutionProfile profile = new ExecutionProfile(
                "team-grid-web",
                TestTarget.WEB_DESKTOP,
                "Team Grid",
                () -> null
        );

        IllegalStateException error = assertThrows(IllegalStateException.class, profile::createConfig);

        assertTrue(error.getMessage().contains("team-grid-web"));
        assertTrue(error.getMessage().contains("null driver config"));
    }
}
