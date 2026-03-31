package io.github.roberto22palomar.pepenium.core.runtime;

import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseTestBehaviorTest {

    @Mock
    private PepeniumRuntime runtime;

    @Mock
    private DriverSession session;

    @Mock
    private WebDriver driver;

    @Mock
    private AppiumDriver appiumDriver;

    @Mock
    private DriverConfig config;

    @AfterEach
    void tearDown() {
        StepTracker.clear();
    }

    @Test
    void appiumDriverSyncsRuntimeStateBeforeReturningDriver() throws Exception {
        RuntimeFixture fixture = new RuntimeFixture();
        attachRuntime(fixture, runtime);
        when(runtime.getSession()).thenReturn(session);
        when(runtime.getDriver()).thenReturn(driver);
        when(runtime.getAppiumDriver()).thenReturn(appiumDriver);

        AppiumDriver resolved = fixture.appiumDriver();

        assertSame(appiumDriver, resolved);
        assertSame(session, fixture.session);
        assertSame(driver, fixture.driver);
    }

    @Test
    void stepRecordsManualObservabilityStep() {
        RuntimeFixture fixture = new RuntimeFixture();

        fixture.step("manual step");

        assertEquals(1, StepTracker.snapshot().getTotalRecorded());
        org.junit.jupiter.api.Assertions.assertTrue(
                StepTracker.snapshot().getSteps().get(0).endsWith("manual step")
        );
    }

    @Test
    void initializeDriverDelegatesToRuntimeAndSyncsState() throws Exception {
        RuntimeFixture fixture = new RuntimeFixture();
        attachRuntime(fixture, runtime);
        when(runtime.getSession()).thenReturn(session);
        when(runtime.getDriver()).thenReturn(driver);

        fixture.initializeDriver(config);

        verify(runtime).initializeDriver(config, TestTarget.WEB_DESKTOP);
        assertSame(session, fixture.session);
        assertSame(driver, fixture.driver);
    }

    @Test
    void initializeDriverForProfileDelegatesToRuntimeAndSyncsState() throws Exception {
        RuntimeFixture fixture = new RuntimeFixture();
        attachRuntime(fixture, runtime);
        when(runtime.getSession()).thenReturn(session);
        when(runtime.getDriver()).thenReturn(driver);

        fixture.initializeDriverForProfile("local-web");

        verify(runtime).initializeDriverForProfile(TestTarget.WEB_DESKTOP, "local-web");
        assertSame(session, fixture.session);
        assertSame(driver, fixture.driver);
    }

    @Test
    void cleanupDriverClearsExposedFieldsAfterRuntimeCleanup() throws Exception {
        RuntimeFixture fixture = new RuntimeFixture();
        attachRuntime(fixture, runtime);
        when(runtime.getSession()).thenReturn(session).thenReturn(null);
        when(runtime.getDriver()).thenReturn(driver).thenReturn(null);

        fixture.syncRuntimeState();
        fixture.cleanupDriver();

        verify(runtime).cleanupDriver();
        assertNull(fixture.session);
        assertNull(fixture.driver);
    }

    @Test
    void runWithConfigAlwaysCleansUpAfterSuccessfulBody() throws Exception {
        RuntimeFixture fixture = new RuntimeFixture();
        attachRuntime(fixture, runtime);
        InOrder inOrder = inOrder(runtime);
        when(runtime.getSession()).thenReturn(session).thenReturn(null);
        when(runtime.getDriver()).thenReturn(driver).thenReturn(null);

        fixture.runWithConfig(config, () -> assertSame(driver, fixture.driver));

        inOrder.verify(runtime).initializeDriver(config, TestTarget.WEB_DESKTOP);
        inOrder.verify(runtime).getSession();
        inOrder.verify(runtime).getDriver();
        inOrder.verify(runtime).cleanupDriver();
        inOrder.verify(runtime).getSession();
        inOrder.verify(runtime).getDriver();
    }

    @Test
    void runWithConfigAlwaysCleansUpAfterFailingBody() throws Exception {
        RuntimeFixture fixture = new RuntimeFixture();
        attachRuntime(fixture, runtime);
        when(runtime.getSession()).thenReturn(session).thenReturn(null);
        when(runtime.getDriver()).thenReturn(driver).thenReturn(null);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> fixture.runWithConfig(config, () -> {
                    throw new IllegalStateException("boom");
                }));

        assertEquals("boom", error.getMessage());
        verify(runtime).cleanupDriver();
        assertNull(fixture.session);
        assertNull(fixture.driver);
    }

    @Test
    void runWithProfileAlwaysCleansUpAfterSuccessfulBody() throws Exception {
        RuntimeFixture fixture = new RuntimeFixture();
        attachRuntime(fixture, runtime);
        InOrder inOrder = inOrder(runtime);
        when(runtime.getSession()).thenReturn(session).thenReturn(null);
        when(runtime.getDriver()).thenReturn(driver).thenReturn(null);

        fixture.runWithProfile("local-web", () -> assertSame(driver, fixture.driver));

        inOrder.verify(runtime).initializeDriverForProfile(TestTarget.WEB_DESKTOP, "local-web");
        inOrder.verify(runtime).getSession();
        inOrder.verify(runtime).getDriver();
        inOrder.verify(runtime).cleanupDriver();
        inOrder.verify(runtime).getSession();
        inOrder.verify(runtime).getDriver();
    }

    @Test
    void runWithProfileAlwaysCleansUpAfterFailingBody() throws Exception {
        RuntimeFixture fixture = new RuntimeFixture();
        attachRuntime(fixture, runtime);
        when(runtime.getSession()).thenReturn(session).thenReturn(null);
        when(runtime.getDriver()).thenReturn(driver).thenReturn(null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> fixture.runWithProfile("local-web", () -> {
                    throw new IllegalArgumentException("bad body");
                }));

        assertEquals("bad body", error.getMessage());
        verify(runtime).cleanupDriver();
        assertNull(fixture.session);
        assertNull(fixture.driver);
    }

    private void attachRuntime(BaseTest fixture, PepeniumRuntime runtime) throws Exception {
        Field runtimeField = BaseTest.class.getDeclaredField("runtime");
        runtimeField.setAccessible(true);
        runtimeField.set(fixture, runtime);
    }

    private static final class RuntimeFixture extends BaseTest {
        @Override
        protected TestTarget getTarget() {
            return TestTarget.WEB_DESKTOP;
        }
    }
}
