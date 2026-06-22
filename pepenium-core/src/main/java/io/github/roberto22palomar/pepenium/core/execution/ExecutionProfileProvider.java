package io.github.roberto22palomar.pepenium.core.execution;

import java.util.Collection;

/**
 * Supplies execution profiles from a consumer application through Java's
 * {@link java.util.ServiceLoader} mechanism.
 */
@FunctionalInterface
public interface ExecutionProfileProvider {

    Collection<ExecutionProfile> profiles();
}
