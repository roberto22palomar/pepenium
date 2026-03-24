package io.github.roberto22palomar.pepenium.core;

public interface DriverSessionFactory {
    DriverSession create(DriverRequest request) throws Exception;
}
