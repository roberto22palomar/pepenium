package io.github.roberto22palomar.pepenium.tests.myProjectExample.android;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.BaseTest;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsApp;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsApp;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.flows.ExampleAndroidShowcaseFlow;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.pages.BottomNavigationPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.android.pages.SearchPage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Slf4j
@Tag("pepenium-example")
public class ExampleAndroidNativeTest extends BaseTest {

    @Override
    protected TestTarget getTarget() {
        return TestTarget.ANDROID_NATIVE;
    }

    @Test
    void basicNavigationFlow_shouldRun() {
        ActionsApp actionsApp = new ActionsApp(appiumDriver());
        AssertionsApp assertionsApp = new AssertionsApp(appiumDriver());
        SearchPage searchPage = new SearchPage(actionsApp);
        BottomNavigationPage bottomNavigationPage = new BottomNavigationPage(actionsApp);
        ExampleAndroidShowcaseFlow flow = new ExampleAndroidShowcaseFlow(
                bottomNavigationPage,
                searchPage,
                assertionsApp,
                this::step
        );
        flow.runShowcaseFlow();
        log.info("Example Android native flow finished");
    }
}
