package io.github.roberto22palomar.pepenium.core.runtime;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.observability.StepTracker;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.WebDriver;

/**
 * Main public authoring entry point for Pepenium test classes.
 *
 * <p>Current contract on the road to {@code 1.0.0}:</p>
 *
 * <ul>
 *     <li>JUnit test instances use {@link TestInstance.Lifecycle#PER_CLASS}.</li>
 *     <li>When automatic lifecycle is enabled, Pepenium creates one managed driver session per test class.</li>
 *     <li>Per-test reporting and observability state is still reset around each test method.</li>
 *     <li>The protected fields and hooks in this type are intended for direct use by test subclasses.</li>
 * </ul>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressFBWarnings(
        value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
        justification = "These protected fields are exposed for test subclasses to use directly."
)
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

    /**
     * Declares the functional target of the test class.
     */
    protected abstract TestTarget getTarget();

    /**
     * Optional default execution profile id for this test class.
     */
    protected String getDefaultProfileId() {
        return null;
    }

    /**
     * Enables or disables the built-in automatic driver lifecycle for this test class.
     */
    protected boolean useAutomaticLifecycle() {
        return true;
    }

    /**
     * Returns the current Appium driver view for mobile-oriented tests.
     */
    protected AppiumDriver appiumDriver() {
        syncRuntimeState();
        return runtime.getAppiumDriver();
    }

    /**
     * Records a manual step in Pepenium observability.
     */
    protected void step(String description) {
        StepTracker.record(description);
    }

    /**
     * Initializes a driver session with an explicit driver config.
     */
    protected void initializeDriver(DriverConfig config) throws Exception {
        runtime.initializeDriver(config, getTarget());
        syncRuntimeState();
    }

    /**
     * Initializes a driver session from an execution profile id.
     */
    protected void initializeDriverForProfile(String profileId) throws Exception {
        runtime.initializeDriverForProfile(getTarget(), profileId);
        syncRuntimeState();
    }

    /**
     * Cleans up the current driver session.
     */
    protected void cleanupDriver() {
        runtime.cleanupDriver();
        syncRuntimeState();
    }

    /**
     * Runs a test body with a temporary driver created from the provided config.
     */
    protected void runWithConfig(DriverConfig config, ThrowingRunnable testBody) throws Exception {
        initializeDriver(config);
        try {
            testBody.run();
        } finally {
            cleanupDriver();
        }
    }

    /**
     * Runs a test body with a temporary driver created from the provided execution profile id.
     */
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
