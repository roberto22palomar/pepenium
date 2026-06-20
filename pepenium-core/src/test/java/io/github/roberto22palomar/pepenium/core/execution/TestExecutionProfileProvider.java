package io.github.roberto22palomar.pepenium.core.execution;

import java.util.Collection;
import java.util.List;

public final class TestExecutionProfileProvider implements ExecutionProfileProvider {

    @Override
    public Collection<ExecutionProfile> profiles() {
        return List.of(new ExecutionProfile(
                "test-service-web",
                TestTarget.WEB_DESKTOP,
                "Profile loaded from a test ServiceLoader provider",
                () -> () -> null
        ));
    }
}
