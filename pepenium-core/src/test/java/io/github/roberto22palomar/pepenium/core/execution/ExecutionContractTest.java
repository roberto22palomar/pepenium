package io.github.roberto22palomar.pepenium.core.execution;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExecutionContractTest {

    @Test
    void testTargetValuesAndDefaultsRemainStable() {
        List<String> targetNames = List.of(
                "ANDROID_NATIVE",
                "ANDROID_WEB",
                "IOS_NATIVE",
                "IOS_WEB",
                "WEB_DESKTOP"
        );

        assertEquals(
                targetNames,
                java.util.Arrays.stream(TestTarget.values()).map(Enum::name).collect(Collectors.toList())
        );
        assertEquals("local-android", TestTarget.ANDROID_NATIVE.getDefaultProfileId());
        assertEquals("local-android-web", TestTarget.ANDROID_WEB.getDefaultProfileId());
        assertNull(TestTarget.IOS_NATIVE.getDefaultProfileId());
        assertNull(TestTarget.IOS_WEB.getDefaultProfileId());
        assertEquals("local-web", TestTarget.WEB_DESKTOP.getDefaultProfileId());
    }

    @Test
    void builtInExecutionProfileIdsAndTargetsRemainStable() {
        Map<String, TestTarget> expectedProfiles = new LinkedHashMap<>();
        expectedProfiles.put("local-android", TestTarget.ANDROID_NATIVE);
        expectedProfiles.put("local-android-web", TestTarget.ANDROID_WEB);
        expectedProfiles.put("local-web", TestTarget.WEB_DESKTOP);
        expectedProfiles.put("local-web-firefox", TestTarget.WEB_DESKTOP);
        expectedProfiles.put("local-web-edge", TestTarget.WEB_DESKTOP);
        expectedProfiles.put("aws-android", TestTarget.ANDROID_NATIVE);
        expectedProfiles.put("aws-android-web", TestTarget.ANDROID_WEB);
        expectedProfiles.put("aws-ios", TestTarget.IOS_NATIVE);
        expectedProfiles.put("browserstack-android", TestTarget.ANDROID_NATIVE);
        expectedProfiles.put("browserstack-android-web", TestTarget.ANDROID_WEB);
        expectedProfiles.put("browserstack-ios", TestTarget.IOS_NATIVE);
        expectedProfiles.put("browserstack-ios-web", TestTarget.IOS_WEB);
        expectedProfiles.put("browserstack-windows-web", TestTarget.WEB_DESKTOP);
        expectedProfiles.put("browserstack-mac-web", TestTarget.WEB_DESKTOP);

        Map<String, TestTarget> actualProfiles = ExecutionProfiles.list().stream()
                .collect(Collectors.toMap(
                        ExecutionProfile::getId,
                        ExecutionProfile::getTarget,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        assertEquals(expectedProfiles, actualProfiles);
    }
}
