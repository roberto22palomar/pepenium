package io.github.roberto22palomar.pepenium.smoke.android;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumTest;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.Test;

@PepeniumTest(target = TestTarget.ANDROID_NATIVE)
class AndroidPublicApiSmokeTest {

    @PepeniumInject
    private AppiumDriver driver;

    @PepeniumInject
    private AndroidShowcaseFlow flow;

    @Test
    void publicAndroidAuthoringApiCompilesForExternalConsumers() {
        if (driver != null && flow != null) {
            flow.openQuickSearch();
        }
    }
}
