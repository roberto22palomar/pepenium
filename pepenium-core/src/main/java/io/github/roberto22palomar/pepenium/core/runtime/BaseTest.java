package io.github.roberto22palomar.pepenium.core.runtime;

import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.WebDriver;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    protected WebDriver driver;
    protected DriverSession session;

    private final PepeniumRuntime runtime = new PepeniumRuntime();

    @RegisterExtension
    final PepeniumLifecycleExtension pepeniumLifecycle = new PepeniumLifecycleExtension(this, runtime);

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
        syncRuntimeState();
        return runtime.getAppiumDriver();
    }

    protected void step(String description) {
        StepTracker.record(description);
    }

    protected void initializeDriver(DriverConfig config) throws Exception {
        runtime.initializeDriver(config, getTarget());
        syncRuntimeState();
    }

    protected void initializeDriverForProfile(String profileId) throws Exception {
        runtime.initializeDriverForProfile(getTarget(), profileId);
        syncRuntimeState();
    }

    protected void cleanupDriver() {
        runtime.cleanupDriver();
        syncRuntimeState();
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

    final void syncRuntimeState() {
        this.session = runtime.getSession();
        this.driver = runtime.getDriver();
    }
}
