package io.github.roberto22palomar.pepenium.core.execution;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Objects;
import java.util.function.Supplier;

@SuppressFBWarnings(
        value = "CT_CONSTRUCTOR_THROW",
        justification = "Constructor validation is intentional; the profile owns no resources and defines no finalizer."
)
public class ExecutionProfile {
    private final String id;
    private final TestTarget target;
    private final String description;
    private final Supplier<DriverConfig> configSupplier;

    public ExecutionProfile(String id, TestTarget target, String description, Supplier<DriverConfig> configSupplier) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Execution profile id is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Execution profile description is required for '" + id.trim() + "'");
        }
        this.id = id.trim();
        this.target = Objects.requireNonNull(target, "Execution profile target is required for '" + this.id + "'");
        this.description = description.trim();
        this.configSupplier = Objects.requireNonNull(
                configSupplier,
                "Execution profile config supplier is required for '" + this.id + "'"
        );
    }

    public DriverConfig createConfig() {
        DriverConfig config = configSupplier.get();
        if (config == null) {
            throw new IllegalStateException("Execution profile '" + id + "' produced a null driver config");
        }
        return config;
    }

    public String getId() {
        return id;
    }

    public TestTarget getTarget() {
        return target;
    }

    public String getDescription() {
        return description;
    }

    public Supplier<DriverConfig> getConfigSupplier() {
        return configSupplier;
    }
}
