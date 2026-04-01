package io.github.roberto22palomar.pepenium.smoke.contract;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumSteps;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumTest;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

@PepeniumTest(target = TestTarget.WEB_DESKTOP)
class AnnotationStyleSmokeTest {

    @PepeniumInject
    private WebDriver driver;

    @PepeniumInject
    private AssertionsWeb assertionsWeb;

    @PepeniumInject
    private PepeniumSteps steps;

    @Test
    void annotationStyleAuthoringCompiles() {
        if (driver != null && assertionsWeb != null && steps != null) {
            steps.step("Annotation-style smoke wiring compiled");
        }
    }
}
