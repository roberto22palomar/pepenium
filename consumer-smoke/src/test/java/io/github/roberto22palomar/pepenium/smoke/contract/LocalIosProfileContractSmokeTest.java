package io.github.roberto22palomar.pepenium.smoke.contract;

import io.github.roberto22palomar.pepenium.core.execution.ExecutionProfiles;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalIosProfileContractSmokeTest {

    @Test
    void localIosProfilesAreAvailableToExternalConsumers() {
        assertEquals("local-ios", TestTarget.IOS_NATIVE.getDefaultProfileId());
        assertEquals("local-ios-web", TestTarget.IOS_WEB.getDefaultProfileId());
        assertTrue(ExecutionProfiles.exists("local-ios"));
        assertTrue(ExecutionProfiles.exists("local-ios-web"));
    }
}
