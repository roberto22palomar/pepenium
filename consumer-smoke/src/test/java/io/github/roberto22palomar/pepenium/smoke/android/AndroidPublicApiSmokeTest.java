package io.github.roberto22palomar.pepenium.smoke.android;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.BaseTest;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsApp;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsApp;
import org.junit.jupiter.api.Test;

class AndroidPublicApiSmokeTest extends BaseTest {

    @Override
    protected TestTarget getTarget() {
        return TestTarget.ANDROID_NATIVE;
    }

    @Override
    protected boolean useAutomaticLifecycle() {
        return false;
    }

    @Test
    void publicAndroidAuthoringApiCompilesForExternalConsumers() {
        ActionsApp actions = new ActionsApp(appiumDriver());
        AssertionsApp assertions = new AssertionsApp(appiumDriver());
        AndroidSearchPage searchPage = new AndroidSearchPage(actions, assertions);
        AndroidShowcaseFlow flow = new AndroidShowcaseFlow(searchPage, this::step);

        flow.openQuickSearch();
    }
}
