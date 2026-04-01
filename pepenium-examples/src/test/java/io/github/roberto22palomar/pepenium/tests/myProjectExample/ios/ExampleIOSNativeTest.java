package io.github.roberto22palomar.pepenium.tests.myProjectExample.ios;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumTest;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.ios.flows.ExampleIOSShowcaseFlow;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Slf4j
@Tag("pepenium-example")
@PepeniumTest(target = TestTarget.IOS_NATIVE, profile = "browserstack-ios")
public class ExampleIOSNativeTest {

    @PepeniumInject
    private ExampleIOSShowcaseFlow flow;

    @Test
    void basicNavigationFlow_shouldRun() {
        flow.runShowcaseFlow();
        log.info("Example iOS native flow finished");
    }
}
