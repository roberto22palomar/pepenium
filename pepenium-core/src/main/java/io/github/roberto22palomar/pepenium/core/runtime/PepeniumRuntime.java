package io.github.roberto22palomar.pepenium.core.runtime;

import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.ExecutionProfile;
import io.github.roberto22palomar.pepenium.core.execution.ExecutionProfileResolver;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.observability.FailureContextReporter;
import io.github.roberto22palomar.pepenium.core.observability.LoggingContext;
import io.github.roberto22palomar.pepenium.core.observability.PepeniumHtmlReportWriter;
import io.github.roberto22palomar.pepenium.core.observability.PepeniumTimeline;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

@Slf4j
final class PepeniumRuntime {

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
        if (session != null) {
            session.close();
            session = null;
        }
        driver = null;
        if (hadSession) {
            lifecycleVersion++;
        }
        LoggingContext.clearAll();
        StepTracker.clear();
    }

    private void openSession(DriverRequest request) throws Exception {
        LoggingContext.setSessionContext(request);
        try {
            session = sessionFactory.create(request);
            driver = session.getDriver();
            lifecycleVersion++;
        } catch (Exception e) {
            LoggingContext.clearAll();
            throw e;
        }
    }
}
