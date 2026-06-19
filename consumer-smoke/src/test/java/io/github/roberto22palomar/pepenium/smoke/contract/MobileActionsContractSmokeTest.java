package io.github.roberto22palomar.pepenium.smoke.contract;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumTest;
import io.github.roberto22palomar.pepenium.toolkit.actions.MobileActions;
import io.github.roberto22palomar.pepenium.toolkit.actions.SwipeDirection;
import io.github.roberto22palomar.pepenium.toolkit.assertions.MobileAssertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

@PepeniumTest(target = TestTarget.ANDROID_NATIVE)
class MobileActionsContractSmokeTest {

    @PepeniumInject
    private MobileActions actions;

    @PepeniumInject
    private MobileAssertions assertions;

    @Test
    void sharedMobileActionsApiCompilesForExternalConsumers() {
        By locator = By.id("shared-mobile-control");
        SwipeDirection direction = SwipeDirection.UP;

        if (actions != null) {
            actions.isElementPresent(locator);
            actions.isElementVisible(locator);
            actions.waitGone(locator);
            actions.swipeAtElement(locator, direction, 1, 0.5, 150);
        }
        if (assertions != null) {
            assertions.assertPresent(locator);
            assertions.assertTextContains(locator, "ready");
        }
    }
}
