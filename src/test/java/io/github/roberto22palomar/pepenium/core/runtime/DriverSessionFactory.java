package io.github.roberto22palomar.pepenium.core.runtime;

import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;

public interface DriverSessionFactory {
    DriverSession create(DriverRequest request) throws Exception;
}
