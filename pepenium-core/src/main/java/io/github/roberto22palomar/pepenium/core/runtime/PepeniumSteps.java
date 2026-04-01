package io.github.roberto22palomar.pepenium.core.runtime;

/**
 * Lightweight step recorder for annotation-first tests, pages and flows.
 */
@FunctionalInterface
public interface PepeniumSteps {

    void step(String description);
}
