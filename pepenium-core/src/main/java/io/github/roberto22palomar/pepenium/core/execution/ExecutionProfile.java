package io.github.roberto22palomar.pepenium.core.execution;

import lombok.Getter;

import java.util.function.Supplier;

@Getter
public class ExecutionProfile {
    private final String id;
    private final TestTarget target;
    private final String description;
    private final Supplier<DriverConfig> configSupplier;

    public ExecutionProfile(String id, TestTarget target, String description, Supplier<DriverConfig> configSupplier) {
        this.id = id;
        this.target = target;
        this.description = description;
        this.configSupplier = configSupplier;
    }

    public DriverConfig createConfig() {
        return configSupplier.get();
    }
}
