package io.github.roberto22palomar.pepenium.core.runtime;

import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.ExecutionProfile;
import io.github.roberto22palomar.pepenium.core.execution.ExecutionProfileResolver;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.config.PepeniumConfig;
import io.github.roberto22palomar.pepenium.core.observability.FailureContextReporter;
import io.github.roberto22palomar.pepenium.core.observability.LoggingContext;
import io.github.roberto22palomar.pepenium.core.observability.PepeniumHtmlReportWriter;
import io.github.roberto22palomar.pepenium.core.observability.PepeniumTimeline;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Objects;

@Slf4j
final class PepeniumRuntime implements ExtensionContext.Store.CloseableResource {

    private final DriverSessionFactory sessionFactory;
    private final ExecutionProfileResolver profileResolver;

    private DriverSession session;
    private WebDriver driver;
    private long lifecycleVersion;

    PepeniumRuntime() {
        this(new DefaultDriverSessionFactory(), new ExecutionProfileResolver());
    }

    PepeniumRuntime(DriverSessionFactory sessionFactory, ExecutionProfileResolver profileResolver) {
        this.sessionFactory = sessionFactory;
        this.profileResolver = profileResolver;
    }

    WebDriver getDriver() {
        return driver;
    }

    DriverSession getSession() {
        return session;
    }

    long getLifecycleVersion() {
        return lifecycleVersion;
    }

    AppiumDriver getAppiumDriver() {
        if (session == null) {
            throw new IllegalStateException("Driver session has not been initialized yet");
        }
        return session.getAppiumDriver();
    }

    void initializeDriver(DriverConfig config, TestTarget target) throws Exception {
        DriverRequest request = config.createRequest()
                .toBuilder()
                .target(target)
                .build();
        openSession(request);
    }

    void initializeDriverForProfile(TestTarget target, String profileId) throws Exception {
        ExecutionProfile profile = profileResolver.resolve(target, profileId);
        try {
            DriverRequest request = profile.createConfig()
                    .createRequest()
                    .toBuilder()
                    .target(target)
                    .executionProfileId(profile.getId())
                    .executionProfileDescription(profile.getDescription())
                    .build();

            log.info("Resolved execution profile '{}' for target '{}' ({})",
                    profile.getId(), target, profile.getDescription());

            openSession(request);
        } catch (Exception error) {
            PepeniumConfig.clearActiveProfile();
            throw error;
        }
    }

    void reportFailure(String displayName, Throwable cause) {
        FailureContextReporter.report(displayName, session, cause);
    }

    void writeTestReport(String displayName, Throwable cause) {
        PepeniumHtmlReportWriter.write(displayName, session, cause);
    }

    void beginTestObservability() {
        PepeniumTimeline.beginTest();
    }

    void clearPerTestState() {
        StepTracker.clear();
    }

    void cleanupDriver() {
        boolean hadSession = session != null || driver != null;
        DriverSession currentSession = session;
        session = null;
        driver = null;
        if (hadSession) {
            lifecycleVersion++;
        }
        try {
            if (currentSession != null) {
                currentSession.close();
            }
        } finally {
            PepeniumConfig.clearActiveProfile();
            LoggingContext.clearAll();
            StepTracker.clear();
        }
    }

    private void openSession(DriverRequest request) throws Exception {
        if (session != null || driver != null) {
            throw new IllegalStateException(
                    "A Pepenium driver session is already active. Clean it up before opening another session."
            );
        }
        LoggingContext.setSessionContext(request);
        DriverSession candidate = null;
        try {
            candidate = Objects.requireNonNull(
                    sessionFactory.create(request),
                    "Driver session factory returned null"
            );
            WebDriver candidateDriver = Objects.requireNonNull(
                    candidate.getDriver(),
                    "Driver session factory returned a session without a driver"
            );
            session = candidate;
            driver = candidateDriver;
            lifecycleVersion++;
        } catch (Exception e) {
            if (candidate != null) {
                try {
                    candidate.close();
                } catch (RuntimeException closeError) {
                    e.addSuppressed(closeError);
                }
            }
            PepeniumConfig.clearActiveProfile();
            LoggingContext.clearAll();
            throw e;
        }
    }

    @Override
    public void close() {
        cleanupDriver();
    }
}
