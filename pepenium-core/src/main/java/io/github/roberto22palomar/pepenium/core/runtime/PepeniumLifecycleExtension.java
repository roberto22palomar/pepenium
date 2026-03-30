package io.github.roberto22palomar.pepenium.core.runtime;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

final class PepeniumLifecycleExtension implements BeforeAllCallback, BeforeEachCallback,
        AfterEachCallback, AfterAllCallback, TestWatcher {

    private final BaseTest owner;
    private final PepeniumRuntime runtime;

    PepeniumLifecycleExtension(BaseTest owner, PepeniumRuntime runtime) {
        this.owner = owner;
        this.runtime = runtime;
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (owner.useAutomaticLifecycle()) {
            runtime.initializeDriverForProfile(owner.getTarget(), owner.getDefaultProfileId());
            owner.syncRuntimeState();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        runtime.clearPerTestState();
        runtime.beginTestObservability();
        owner.syncRuntimeState();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        runtime.writeTestReport(context.getDisplayName(), context.getExecutionException().orElse(null));
        runtime.clearPerTestState();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (owner.useAutomaticLifecycle()) {
            runtime.cleanupDriver();
            owner.syncRuntimeState();
        }
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        runtime.reportFailure(context.getDisplayName(), cause);
    }
}
