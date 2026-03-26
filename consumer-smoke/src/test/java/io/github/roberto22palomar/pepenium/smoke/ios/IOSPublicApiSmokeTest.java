package io.github.roberto22palomar.pepenium.smoke.ios;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.BaseTest;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsAppIOS;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsAppIOS;
import org.junit.jupiter.api.Test;

class IOSPublicApiSmokeTest extends BaseTest {

    @Override
    protected TestTarget getTarget() {
        return TestTarget.IOS_NATIVE;
    }

    @Override
    protected boolean useAutomaticLifecycle() {
        return false;
    }

    @Test
    void publicIOSAuthoringApiCompilesForExternalConsumers() {
        ActionsAppIOS actions = new ActionsAppIOS(appiumDriver());
        AssertionsAppIOS assertions = new AssertionsAppIOS(appiumDriver());
        IOSSearchPage searchPage = new IOSSearchPage(actions, assertions);
        IOSShowcaseFlow flow = new IOSShowcaseFlow(searchPage, this::step);

        flow.openQuickSearch();
    }
}
