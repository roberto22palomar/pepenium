package io.github.roberto22palomar.pepenium.core.runtime;

import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class PepeniumLifecycleExtensionTest {

    @Mock
    private PepeniumRuntime runtime;

    @Mock
    private DriverSession session;

    @Mock
    private WebDriver driver;

    @Mock
    private ExtensionContext context;

    @Mock
    private Throwable failure;

    @Test
    void automaticLifecycleInitializesOncePerClassAndCleansUpAfterAll() throws Exception {
        AutomaticFixture owner = new AutomaticFixture();
        attachRuntime(owner, runtime);
        PepeniumLifecycleExtension extension = new PepeniumLifecycleExtension(owner, runtime);
        when(runtime.getSession()).thenReturn(session, session, null);
        when(runtime.getDriver()).thenReturn(driver, driver, null);

        extension.beforeAll(context);
        extension.beforeEach(context);
        extension.afterEach(context);
        extension.beforeEach(context);
        extension.afterEach(context);
        extension.afterAll(context);

        verify(runtime).initializeDriverForProfile(TestTarget.WEB_DESKTOP, "local-web");
        verify(runtime).cleanupDriver();
        verify(runtime, times(2)).beginTestObservability();
        verify(runtime, times(4)).clearPerTestState();
        assertNull(owner.session);
        assertNull(owner.driver);
    }

    @Test
    void manualLifecycleSkipsAutomaticSessionCreationAndCleanup() throws Exception {
        ManualFixture owner = new ManualFixture();
        attachRuntime(owner, runtime);
        PepeniumLifecycleExtension extension = new PepeniumLifecycleExtension(owner, runtime);
        when(runtime.getSession()).thenReturn(null);
        when(runtime.getDriver()).thenReturn(null);

        extension.beforeAll(context);
        extension.beforeEach(context);
        extension.afterEach(context);
        extension.afterAll(context);

        verify(runtime, never()).initializeDriverForProfile(TestTarget.WEB_DESKTOP, null);
        verify(runtime, never()).cleanupDriver();
    }

    @Test
    void beforeEachClearsPerTestStateBeforeStartingObservabilityAndSyncsOwnerState() throws Exception {
        AutomaticFixture owner = new AutomaticFixture();
        attachRuntime(owner, runtime);
        PepeniumLifecycleExtension extension = new PepeniumLifecycleExtension(owner, runtime);
        InOrder inOrder = inOrder(runtime);
        when(runtime.getSession()).thenReturn(session);
        when(runtime.getDriver()).thenReturn(driver);

        extension.beforeEach(context);

        inOrder.verify(runtime).clearPerTestState();
        inOrder.verify(runtime).beginTestObservability();
        inOrder.verify(runtime).getSession();
        inOrder.verify(runtime).getDriver();
        assertSame(session, owner.session);
        assertSame(driver, owner.driver);
    }

    @Test
    void afterEachWritesReportThenClearsPerTestStateUsingContextData() throws Exception {
        AutomaticFixture owner = new AutomaticFixture();
        attachRuntime(owner, runtime);
        PepeniumLifecycleExtension extension = new PepeniumLifecycleExtension(owner, runtime);
        InOrder inOrder = inOrder(runtime);
        when(context.getDisplayName()).thenReturn("contract test");
        when(context.getExecutionException()).thenReturn(Optional.of(failure));

        extension.afterEach(context);

        inOrder.verify(runtime).writeTestReport("contract test", failure);
        inOrder.verify(runtime).clearPerTestState();
    }

    @Test
    void testFailedDelegatesFailureReportingWithDisplayName() throws Exception {
        AutomaticFixture owner = new AutomaticFixture();
        attachRuntime(owner, runtime);
        PepeniumLifecycleExtension extension = new PepeniumLifecycleExtension(owner, runtime);
        when(context.getDisplayName()).thenReturn("contract test");

        extension.testFailed(context, failure);

        verify(runtime).reportFailure("contract test", failure);
    }

    private void attachRuntime(BaseTest fixture, PepeniumRuntime runtime) throws Exception {
        Field runtimeField = BaseTest.class.getDeclaredField("runtime");
        runtimeField.setAccessible(true);
        runtimeField.set(fixture, runtime);
    }

    private static final class AutomaticFixture extends BaseTest {
        @Override
        protected TestTarget getTarget() {
            return TestTarget.WEB_DESKTOP;
        }

        @Override
        protected String getDefaultProfileId() {
            return "local-web";
        }
    }

    private static final class ManualFixture extends BaseTest {
        @Override
        protected TestTarget getTarget() {
            return TestTarget.WEB_DESKTOP;
        }

        @Override
        protected boolean useAutomaticLifecycle() {
            return false;
        }
    }
}
