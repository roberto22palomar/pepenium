package io.github.roberto22palomar.pepenium.tests.myProjectExample.android;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumTest;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.flows.ExampleAndroidShowcaseFlow;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Slf4j
@Tag("pepenium-example")
@PepeniumTest(target = TestTarget.ANDROID_NATIVE)
public class ExampleAndroidNativeTest {

    @PepeniumInject
    private ExampleAndroidShowcaseFlow flow;

    @Test
    void basicNavigationFlow_shouldRun() {
        flow.runShowcaseFlow();
        log.info("Example Android native flow finished");
    }
}
