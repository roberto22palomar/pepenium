package io.github.roberto22palomar.pepenium.core.runtime;

import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import io.github.roberto22palomar.pepenium.core.execution.ExecutionProfile;
import io.github.roberto22palomar.pepenium.core.execution.ExecutionProfileResolver;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PepeniumRuntimeTest {

    @AfterEach
    void tearDown() {
        StepTracker.clear();
    }

    @Test
    void initializeDriverBuildsRequestForTarget() throws Exception {
        RecordingSessionFactory sessionFactory = new RecordingSessionFactory();
        PepeniumRuntime runtime = new PepeniumRuntime(sessionFactory, new ExecutionProfileResolver());

        runtime.initializeDriver(() -> DriverRequest.builder()
                .driverType(DriverType.LOCAL_CHROME)
                .capabilities(capabilities())
                .description("test config")
                .build(), TestTarget.WEB_DESKTOP);

        assertSame(sessionFactory.session, runtime.getSession());
        assertSame(sessionFactory.session.getDriver(), runtime.getDriver());
        assertEquals(TestTarget.WEB_DESKTOP, sessionFactory.capturedRequest.getTarget());
        assertEquals("test config", sessionFactory.capturedRequest.getDescription());
    }

    @Test
    void initializeDriverForProfileEnrichesRequestWithProfileMetadata() throws Exception {
        RecordingSessionFactory sessionFactory = new RecordingSessionFactory();
        FakeExecutionProfileResolver resolver = new FakeExecutionProfileResolver(
                new ExecutionProfile(
                        "custom-profile",
                        TestTarget.ANDROID_NATIVE,
                        "Custom Android profile",
                        () -> (DriverConfig) () -> DriverRequest.builder()
                                .driverType(DriverType.ANDROID_APPIUM)
                                .capabilities(capabilities())
                                .description("android config")
                                .build()
                )
        );
        PepeniumRuntime runtime = new PepeniumRuntime(sessionFactory, resolver);

        runtime.initializeDriverForProfile(TestTarget.ANDROID_NATIVE, null);

        assertEquals("custom-profile", sessionFactory.capturedRequest.getExecutionProfileId());
        assertEquals("Custom Android profile", sessionFactory.capturedRequest.getExecutionProfileDescription());
        assertEquals(TestTarget.ANDROID_NATIVE, sessionFactory.capturedRequest.getTarget());
    }

    @Test
    void cleanupDriverClosesSessionAndClearsTrackedState() throws Exception {
        RecordingSessionFactory sessionFactory = new RecordingSessionFactory();
        PepeniumRuntime runtime = new PepeniumRuntime(sessionFactory, new ExecutionProfileResolver());

        runtime.initializeDriver(() -> DriverRequest.builder()
                .driverType(DriverType.LOCAL_CHROME)
                .capabilities(capabilities())
                .description("cleanup config")
                .build(), TestTarget.WEB_DESKTOP);
        StepTracker.record("Something happened");

        runtime.cleanupDriver();

        assertTrue(sessionFactory.session.closed);
        assertNull(runtime.getSession());
        assertNull(runtime.getDriver());
        assertEquals(0, StepTracker.snapshot().getTotalRecorded());
    }

    private Capabilities capabilities() {
        return new MutableCapabilities();
    }

    private static final class RecordingSessionFactory implements DriverSessionFactory {
        private DriverRequest capturedRequest;
        private final RecordingDriverSession session = new RecordingDriverSession();

        @Override
        public DriverSession create(DriverRequest request) {
            this.capturedRequest = request;
            return session;
        }
    }

    private static final class RecordingDriverSession extends DriverSession {
        private boolean closed;

        private RecordingDriverSession() {
            super(noOpDriver(), DriverRequest.builder()
                    .driverType(DriverType.LOCAL_CHROME)
                    .capabilities(new MutableCapabilities())
                    .description("recording")
                    .build());
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    private static final class FakeExecutionProfileResolver extends ExecutionProfileResolver {
        private final ExecutionProfile profile;

        private FakeExecutionProfileResolver(ExecutionProfile profile) {
            this.profile = profile;
        }

        @Override
        public ExecutionProfile resolve(TestTarget target, String defaultProfileId) {
            return profile;
        }
    }

    private static WebDriver noOpDriver() {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("quit".equals(method.getName())) {
                return null;
            }
            if (method.getReturnType().equals(void.class)) {
                return null;
            }
            if (method.getReturnType().isPrimitive()) {
                if (method.getReturnType().equals(boolean.class)) {
                    return false;
                }
                if (method.getReturnType().equals(long.class)) {
                    return 0L;
                }
                if (method.getReturnType().equals(int.class)) {
                    return 0;
                }
                if (method.getReturnType().equals(double.class)) {
                    return 0d;
                }
            }
            return null;
        };
        return (WebDriver) Proxy.newProxyInstance(
                WebDriver.class.getClassLoader(),
                new Class[]{WebDriver.class},
                handler
        );
    }
}
