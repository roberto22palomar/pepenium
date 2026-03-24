package io.github.roberto22palomar.pepenium.core;

import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.WebDriver;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public abstract class BaseTest {

    protected WebDriver driver;
    protected DriverSession session;

    private final DriverSessionFactory sessionFactory = new DefaultDriverSessionFactory();
    private final ExecutionProfileResolver profileResolver = new ExecutionProfileResolver();

    @FunctionalInterface
    protected interface ThrowingRunnable {
        void run() throws Exception;
    }

    protected abstract TestTarget getTarget();

    protected String getDefaultProfileId() {
        return null;
    }

    protected boolean useAutomaticLifecycle() {
        return true;
    }

    protected AppiumDriver appiumDriver() {
        if (session == null) {
            throw new IllegalStateException("Driver session has not been initialized yet");
        }
        return session.getAppiumDriver();
    }

    protected void initializeDriver(DriverConfig config) throws Exception {
        DriverRequest request = config.createRequest()
                .toBuilder()
                .target(getTarget())
                .build();
        LoggingContext.setSessionContext(request);
        session = sessionFactory.create(request);
        driver = session.getDriver();
    }

    protected void initializeDriverForProfile(String profileId) throws Exception {
        ExecutionProfile profile = profileResolver.resolve(getTarget(), profileId);
        DriverRequest request = profile.createConfig()
                .createRequest()
                .toBuilder()
                .target(getTarget())
                .executionProfileId(profile.getId())
                .executionProfileDescription(profile.getDescription())
                .build();

        log.info("Resolved execution profile '{}' for target '{}' ({})",
                profile.getId(), getTarget(), profile.getDescription());

        LoggingContext.setSessionContext(request);
        session = sessionFactory.create(request);
        driver = session.getDriver();
    }

    protected void cleanupDriver() {
        if (session != null) {
            session.close();
            session = null;
        }
        driver = null;
        LoggingContext.clearAll();
    }

    protected void runWithConfig(DriverConfig config, ThrowingRunnable testBody) throws Exception {
        initializeDriver(config);
        try {
            testBody.run();
        } finally {
            cleanupDriver();
        }
    }

    protected void runWithProfile(String profileId, ThrowingRunnable testBody) throws Exception {
        initializeDriverForProfile(profileId);
        try {
            testBody.run();
        } finally {
            cleanupDriver();
        }
    }

    @BeforeAll
    void setupSuite() throws Exception {
        if (useAutomaticLifecycle()) {
            initializeDriverForProfile(getDefaultProfileId());
        }
    }

    @AfterAll
    void teardownSuite() {
        if (useAutomaticLifecycle()) {
            cleanupDriver();
        }
    }
}
