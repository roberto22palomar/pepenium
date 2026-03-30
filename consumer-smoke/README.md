# Consumer Smoke

This project is intentionally outside the main Maven reactor.

Its purpose is to validate that Pepenium can be consumed from a separate project using the documented public API, instead of only compiling inside the framework repository itself.

## Typical Validation Flow

Install the local framework artifacts first:

```bash
mvn -q -pl pepenium-core,pepenium-toolkit -am install -DskipTests
```

Then compile the smoke consumer:

```bash
mvn -q -U -f consumer-smoke/pom.xml clean test-compile
```

If needed, the consumed version can be overridden:

```bash
mvn -q -U -f consumer-smoke/pom.xml clean test-compile -Dpepenium.version=0.8.0
```

## What It Covers

The smoke sources intentionally model the expected user-facing workflow:

- extending `BaseTest`
- declaring a `TestTarget`
- building page objects and flows with `Actions*`
- using `Assertions*`
- enriching steps with `step("...")`

The tests are designed for compilation validation, not live execution.

This same flow is intended to run in CI so public API consumption stays protected over time.
