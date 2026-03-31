# Public API Guide

This document defines the intended public API surface of Pepenium in its current pre-`1.0.0` line.

Its goal is simple:

- make it clear which classes external users are expected to build against
- make internal refactors safer for maintainers
- define what should count as a breaking change on the road to `1.0.0`

## Current Status

Pepenium is still pre-`1.0.0`.

That means some areas may still evolve quickly, but not every package should be treated as unstable. This guide marks:

- **Public API**: classes intended for direct use in user tests and framework consumption
- **Advanced / evolving API**: classes that may be used by advanced adopters, but are not yet promised as fully stable
- **Internal API**: framework internals that should not be treated as a compatibility contract

## Public API

These are the main classes Pepenium users are expected to import and rely on directly.

### Core test author API

- [BaseTest](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/runtime/BaseTest.java)
- [TestTarget](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/TestTarget.java)

These classes define the main authoring model:

- one test per functional target
- one execution profile per environment/provider choice
- manual step enrichment through `step("...")`

### Toolkit authoring API

- [ActionsWeb](../pepenium-toolkit/src/main/java/io/github/roberto22palomar/pepenium/toolkit/actions/ActionsWeb.java)
- [ActionsApp](../pepenium-toolkit/src/main/java/io/github/roberto22palomar/pepenium/toolkit/actions/ActionsApp.java)
- [ActionsAppIOS](../pepenium-toolkit/src/main/java/io/github/roberto22palomar/pepenium/toolkit/actions/ActionsAppIOS.java)
- [AssertionsWeb](../pepenium-toolkit/src/main/java/io/github/roberto22palomar/pepenium/toolkit/assertions/AssertionsWeb.java)
- [AssertionsApp](../pepenium-toolkit/src/main/java/io/github/roberto22palomar/pepenium/toolkit/assertions/AssertionsApp.java)
- [AssertionsAppIOS](../pepenium-toolkit/src/main/java/io/github/roberto22palomar/pepenium/toolkit/assertions/AssertionsAppIOS.java)

These are intended as the reusable building blocks for writing tests, flows and page objects.

For cross-platform consistency, the supported cross-platform action surface should converge on the same core verbs across web, Android and iOS, especially `click(...)`, `clickIfVisible(...)`, `type(...)` and `waitUntilHidden(...)`.

The shared assertion surface should likewise stay aligned around `assertVisible(...)`, `assertNotVisible(...)`, `assertPresent(...)`, `assertTextEquals(...)` and `assertTextContains(...)`, while web-only assertions should be reserved for browser-specific concerns such as URL, title and input-value checks.

## Advanced / Evolving API

These classes are visible and may be useful to advanced adopters, but should be considered less stable until the `1.0.0` contract is finalized.

### Execution and configuration types

- [DriverConfig](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/DriverConfig.java)
- [DriverRequest](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/DriverRequest.java)
- [DriverType](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/DriverType.java)
- [ExecutionProfile](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/ExecutionProfile.java)
- [ExecutionProfileResolver](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/ExecutionProfileResolver.java)
- [ExecutionProfiles](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/ExecutionProfiles.java)

### Configuration models and loaders

- `core/config/browserstack/...`
- `core/config/yaml/...`
- `core/configs/local/...`
- `core/configs/browserstack/...`
- `core/configs/aws/...`

These areas are important, but they are still part of the framework evolution space. Pepenium may refactor them further before promising long-term API stability.

Built-in execution profile ids are part of the external launch model, but the internal wiring behind them is still evolving. In particular, the `configKey` values inside `execution-profiles.yml` and the concrete `DriverConfig` implementations should still be treated as internal framework details until Pepenium explicitly promotes them.

## Internal API

These classes support framework lifecycle, observability and driver wiring, but should not be treated as external extension points.

### Runtime internals

- [DriverSession](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/runtime/DriverSession.java)
- [DriverSessionFactory](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/runtime/DriverSessionFactory.java)
- [DefaultDriverSessionFactory](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/runtime/DefaultDriverSessionFactory.java)

### Observability internals

- [StepTracker](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/observability/StepTracker.java)
- [FailureContextReporter](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/observability/FailureContextReporter.java)
- [LoggingContext](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/observability/LoggingContext.java)
- [LoggingPreferences](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/observability/LoggingPreferences.java)
- [CapabilitiesSummary](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/observability/CapabilitiesSummary.java)
- [PepeniumBanner](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/observability/PepeniumBanner.java)

### Toolkit support internals

- [BaseAssertions](../pepenium-toolkit/src/main/java/io/github/roberto22palomar/pepenium/toolkit/assertions/BaseAssertions.java)
- [ActionLoggingSupport](../pepenium-toolkit/src/main/java/io/github/roberto22palomar/pepenium/toolkit/support/ActionLoggingSupport.java)
- [FastUiSettle](../pepenium-toolkit/src/main/java/io/github/roberto22palomar/pepenium/toolkit/support/FastUiSettle.java)
- [ScrollUtils](../pepenium-toolkit/src/main/java/io/github/roberto22palomar/pepenium/toolkit/support/ScrollUtils.java)

Even when these classes are public in Java terms, they should currently be treated as framework internals unless Pepenium documentation explicitly promotes them for direct consumption.

## Breaking Change Guidance

Until `1.0.0`, Pepenium may still make structural improvements. Even so, the project should already treat the following as breaking changes:

- changing the expected usage model of [BaseTest](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/runtime/BaseTest.java)
- removing or renaming [TestTarget](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/TestTarget.java) values
- removing or renaming public `Actions*` methods that user page objects and flows are expected to call
- removing or renaming public `Assertions*` methods that user tests are expected to call
- changing the default execution-profile resolution model in a way that breaks existing test launch setups

For `BaseTest`, the protected authoring hooks used by test subclasses should also be treated as part of the compatibility contract, not only its public type declaration.

## Automated Compatibility Gate

Pepenium now runs an automatic `japicmp` comparison during `verify` for the released `pepenium` and `pepenium-toolkit` artifacts.

That build-time compatibility gate is intentionally scoped to the documented public API surface:

- `pepenium-core`: `BaseTest` and `TestTarget`
- `pepenium-toolkit`: the documented `Actions*` and `Assertions*` authoring types

This keeps the compatibility check focused on what normal external users are expected to import directly, while advanced/evolving and internal areas can still change until they are explicitly promoted into the stable contract.

## Deprecation Policy

Before `1.0.0`, Pepenium should still prefer a deprecation-first policy for documented public API changes whenever that is practical:

- add `@Deprecated` and document the preferred replacement before removing or renaming a public API member
- keep deprecated public API available for at least one released version when the design allows it
- record notable deprecations and planned removals in `CHANGELOG.md`
- only skip the deprecation step for correctness, security or impossible-to-preserve designs, and call that out explicitly as a breaking change

## Practical Rule For Contributors

If a class is not needed for a normal Pepenium user to:

- define a target
- write a test
- write page objects or flows
- use standard toolkit actions and assertions

then it should usually be assumed internal unless Pepenium documentation explicitly says otherwise.

## Road To 1.0.0

Before `1.0.0`, Pepenium should tighten this guide further by:

- validating the public API from an external consumer project
- keeping the automatic binary/source compatibility gate aligned with the documented public API list
- deciding whether execution/configuration types should become stable public API
- deciding whether `BaseTest` remains the main public entry point or evolves into a more annotation/extension-driven JUnit model
- documenting compatibility expectations between minor releases

## Consumer Smoke Validation

Pepenium now includes a standalone smoke consumer under [consumer-smoke](../consumer-smoke/README.md).

That smoke project is intentionally outside the main Maven reactor so it behaves more like an external consumer.

Typical validation flow:

```bash
mvn -q -pl pepenium-core,pepenium-toolkit -am install -DskipTests
mvn -q -U -f consumer-smoke/pom.xml clean test-compile
```
