package io.github.roberto22palomar.pepenium.smoke.ios;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumTest;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.Test;

@PepeniumTest(target = TestTarget.IOS_NATIVE)
class IOSPublicApiSmokeTest {

    @PepeniumInject
    private AppiumDriver driver;

    @PepeniumInject
    private IOSShowcaseFlow flow;

    @Test
    void publicIOSAuthoringApiCompilesForExternalConsumers() {
        if (driver != null && flow != null) {
            flow.openQuickSearch();
        }
    }
}
