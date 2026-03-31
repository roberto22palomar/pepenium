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

## Contract Decisions For 1.0.0

Pepenium now treats the following decisions as explicit contract on the road to `1.0.0`.

### `BaseTest` stays the main public entry point

`BaseTest` remains the primary authoring entry point for normal Pepenium users in `1.0.0`.

That contract includes:

- declaring the target through `getTarget()`
- optionally overriding `getDefaultProfileId()`
- optionally overriding `useAutomaticLifecycle()`
- using `step("...")` for manual observability
- using `appiumDriver()`, `initializeDriver(...)`, `initializeDriverForProfile(...)`, `cleanupDriver()`, `runWithConfig(...)` and `runWithProfile(...)`
- the protected convenience handles `driver` and `session`
- the nested `BaseTest.ThrowingRunnable` helper used by the lifecycle wrappers

### Automatic lifecycle semantics are fixed

For `BaseTest`, Pepenium now treats this lifecycle model as intentional contract:

- test instances use JUnit `PER_CLASS`
- when `useAutomaticLifecycle()` returns `true`, Pepenium creates one managed driver session in `beforeAll`
- that managed session is cleaned in `afterAll`
- per-test observability and reporting state still resets in `beforeEach` / `afterEach`

If a user needs a different lifecycle, the supported opt-out is still `useAutomaticLifecycle() == false` plus the existing manual lifecycle hooks.

### `TestTarget` values and defaults are stable

The current `TestTarget` values are now treated as the stable functional target vocabulary for `1.0.0`:

- `ANDROID_NATIVE` -> default profile `local-android`
- `ANDROID_WEB` -> default profile `local-android-web`
- `IOS_NATIVE` -> no built-in default profile
- `IOS_WEB` -> no built-in default profile
- `WEB_DESKTOP` -> default profile `local-web`

Removing, renaming or silently repointing these defaults should now be treated as a breaking change.

### Built-in execution profile ids are part of the public launch contract

The built-in execution profile ids defined in `execution-profiles.yml` are now treated as stable launch inputs for `1.0.0`:

- `local-android`
- `local-android-web`
- `local-web`
- `local-web-firefox`
- `local-web-edge`
- `aws-android`
- `aws-android-web`
- `aws-ios`
- `browserstack-android`
- `browserstack-android-web`
- `browserstack-ios`
- `browserstack-ios-web`
- `browserstack-windows-web`
- `browserstack-mac-web`

The ids above are contract. The internal wiring behind them is not. The `configKey` values in `execution-profiles.yml` and the concrete `DriverConfig` implementations remain internal framework details.

### `Actions*` and `Assertions*` stay the stable authoring surface

The documented `ActionsWeb`, `ActionsApp`, `ActionsAppIOS`, `AssertionsWeb`, `AssertionsApp` and `AssertionsAppIOS` types are now the intended stable authoring surface for `1.0.0`.

That means public methods on those classes should follow the existing deprecation-first policy instead of being renamed or removed directly.

### Reporting JSON is not yet a stable public schema

The reporting bundle is a supported feature, but the JSON payloads are still treated as evolving diagnostics artifacts for now.

Current decision:

- `index.html` and per-test HTML reports are supported user-facing diagnostics outputs
- `summary.json` and `report-*.json` are useful machine-readable artifacts, but their schema is not yet promised as a stable public API contract
- if Pepenium wants to make reporting JSON stable later, it should first introduce explicit schema versioning and compatibility rules

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

These classes are visible and may be useful to advanced adopters, but should be considered less stable than the main authoring contract.

### Execution and configuration types

- [DriverConfig](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/DriverConfig.java)
- [DriverRequest](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/DriverRequest.java)
- [DriverType](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/DriverType.java)
- [ExecutionProfile](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/ExecutionProfile.java)
- [ExecutionProfileResolver](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/ExecutionProfileResolver.java)
- [ExecutionProfiles](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/ExecutionProfiles.java)
- [DriverSession](../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/runtime/DriverSession.java)

### Configuration models and loaders

- `core/config/browserstack/...`
- `core/config/yaml/...`
- `core/configs/local/...`
- `core/configs/browserstack/...`
- `core/configs/aws/...`

These areas are important, but they are still part of the framework evolution space. Pepenium may refactor them further behind the stable authoring contract.

## Internal API

These classes support framework lifecycle, observability and driver wiring, but should not be treated as external extension points.

### Runtime internals

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
- changing or removing built-in execution profile ids that users may already pass through `-Dpepenium.profile` or `PEPENIUM_PROFILE`

For `BaseTest`, the protected authoring hooks used by test subclasses should also be treated as part of the compatibility contract, not only its public type declaration.

## Automated Compatibility Gate

Pepenium now runs an automatic `japicmp` comparison during `verify` for the released `pepenium` and `pepenium-toolkit` artifacts.

That build-time compatibility gate is intentionally scoped to the documented public API surface:

- `pepenium-core`: `BaseTest` and `TestTarget`
- `pepenium-toolkit`: the documented `Actions*` and `Assertions*` authoring types

This keeps the compatibility check focused on what normal external users are expected to import directly, while semantic contract details such as lifecycle defaults, target defaults and built-in profile ids are protected by dedicated tests and docs.

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
- deciding whether any advanced execution/configuration types should be promoted beyond their current status
- deciding whether reporting JSON should eventually gain a versioned stable schema
- documenting compatibility expectations between minor releases

## Consumer Smoke Validation

Pepenium now includes a standalone smoke consumer under [consumer-smoke](../consumer-smoke/README.md).

That smoke project is intentionally outside the main Maven reactor so it behaves more like an external consumer.

Typical validation flow:

```bash
mvn -q -pl pepenium-core,pepenium-toolkit -am install -DskipTests
mvn -q -U -f consumer-smoke/pom.xml clean test-compile
```
