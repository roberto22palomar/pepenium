<p align="center">
  <a href="https://github.com/roberto22palomar/pepenium/actions/workflows/ci-build.yml">
    <img alt="Build" src="https://github.com/roberto22palomar/pepenium/actions/workflows/ci-build.yml/badge.svg" />
  </a>
</p>

<p align="center">
  <a href="LICENSE">
    <img alt="License" src="https://img.shields.io/badge/License-MIT-green.svg" />
  </a>
  <img alt="Java" src="https://img.shields.io/badge/Java-11-blue.svg" />
  <img alt="Maven" src="https://img.shields.io/badge/Maven-3.x-orange.svg" />
  <img alt="JUnit" src="https://img.shields.io/badge/JUnit-5-purple.svg" />
  <img alt="Selenium" src="https://img.shields.io/badge/Selenium-4-43B02A.svg" />
  <img alt="Appium Client" src="https://img.shields.io/badge/Appium%20Client-10-00BFFF.svg" />
</p>

# Pepenium

<p align="center">
  <strong>English</strong> |
  <a href="README.es.md">Espanol</a>
</p>

Pepenium is a Java automation framework for Android, iOS and Web built on top of Appium, Selenium and JUnit 5.

Its current direction is simple to understand and practical to run: tests declare a functional target, execution profiles decide where they run, and the framework owns session lifecycle, logging and failure diagnostics.

## Why Pepenium

- One test per functional target, not one test per provider
- Shared execution model for local, BrowserStack and AWS Device Farm
- Centralized driver/session lifecycle through a single session factory
- Reusable `Actions*` helpers for Web, Android and iOS
- Screenshot helpers designed for fast flows without blurred captures
- Cleaner logs with automatic runtime context and failure evidence

See [QUICK-START.md](QUICK-START.md) for the fastest way to run it and [CHANGELOG.md](CHANGELOG.md) for release history.
Use [ENVIRONMENT.md](ENVIRONMENT.md) as the central reference for environment variables and runtime properties.
Use [API.md](API.md) for the current public-vs-internal API guidance on the road to `1.0.0`.
Use the root `docker-compose.yaml` if you want to run the local Appium server in Docker while keeping the Android emulator on the host.
Use [consumer-smoke/README.md](consumer-smoke/README.md) for the standalone public-API consumer smoke validation flow.

The main CI workflow now validates that standalone consumer smoke before the packaging job, so public API consumption is checked continuously and not only by local convention.

## What v0.7.0 Adds

- Split the repository into `pepenium-core`, `pepenium-toolkit` and `pepenium-examples`
- Moved framework runtime code into real `src/main` production sources
- Moved BrowserStack config models and YAML loading into `core`
- Kept `toolkit` focused on reusable authoring helpers such as actions and support utilities
- Kept example tests and page objects isolated in `pepenium-examples`
- Preserved root-level build, test and example packaging workflows across the modular structure
- Updated CI so GitHub Actions runs framework tests before packaging

## Current Architecture

Repository modules:

- `pepenium-core`: framework engine, runtime, execution and provider configuration
- `pepenium-toolkit`: reusable test-author helpers such as actions and support utilities
- `pepenium-examples`: example tests, flows and page objects

### `core`

Framework runtime and execution pieces:

- `BaseTest`
- `DriverConfig`
- `DriverRequest`
- `DriverSession`
- `DriverSessionFactory`
- `DefaultDriverSessionFactory`
- `ExecutionProfile`
- `ExecutionProfiles`
- `ExecutionProfileResolver`
- `FailureContextReporter`
- `LoggingContext`
- `PepeniumBanner`
- `StepTracker`
- `TestTarget`
- `core/config/browserstack`: BrowserStack config models
- `core/config/yaml`: YAML loaders for BrowserStack catalogs

Provider-specific request builders currently live under:

- `core/configs/local`
- `core/configs/browserstack`
- `core/configs/aws`

### `toolkit`

Reusable building blocks:

- `toolkit/actions`: `ActionsWeb`, `ActionsApp`, `ActionsAppIOS`
- `toolkit/assertions`: `AssertionsWeb`, `AssertionsApp`, `AssertionsAppIOS`
- `toolkit/support`: reusable settle and scroll helpers

### `examples`

Example tests showing the intended usage pattern:

- `pepenium-examples/src/test/java/.../tests/myProjectExample/android`
- `pepenium-examples/src/test/java/.../tests/myProjectExample/ios`
- `pepenium-examples/src/test/java/.../tests/myProjectExample/web`

Examples are grouped by functional target instead of by environment.

## Execution Model

Tests declare a `TestTarget`:

```java
public class ExampleAndroidNativeTest extends BaseTest {

    @Override
    protected TestTarget getTarget() {
        return TestTarget.ANDROID_NATIVE;
    }
}
```

At runtime, Pepenium resolves an execution profile:

- from `-Dpepenium.profile=...`
- or from `PEPENIUM_PROFILE`
- or from the target default profile when one exists
- with built-in profile metadata loaded from `pepenium-core/src/main/resources/execution-profiles.yml`

This keeps the same test portable across environments without changing its code.

## Supported Targets

- `ANDROID_NATIVE`
- `ANDROID_WEB`
- `IOS_NATIVE`
- `IOS_WEB`
- `WEB_DESKTOP`

## Built-In Execution Profiles

- `local-android`
- `local-android-web`
- `local-web`
- `aws-android`
- `aws-android-web`
- `aws-ios`
- `browserstack-android`
- `browserstack-android-web`
- `browserstack-ios`
- `browserstack-ios-web`
- `browserstack-windows-web`
- `browserstack-mac-web`

The built-in profile catalog is defined in:

- `pepenium-core/src/main/resources/execution-profiles.yml`

## Example Tests

- Android native: [ExampleAndroidNativeTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidNativeTest.java)
- Android web: [ExampleAndroidWebTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidWebTest.java)
- iOS native: [ExampleIOSNativeTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSNativeTest.java)
- iOS web: [ExampleIOSWebTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSWebTest.java)
- Desktop web: [ExampleDesktopWebTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/web/ExampleDesktopWebTest.java)

### Web Showcase Example

The web examples are now functional live examples against [The Internet](https://the-internet.herokuapp.com/), not only structural templates.

The desktop/mobile-web showcase currently demonstrates:

- profile-driven execution with the same test class
- reusable `ActionsWeb` and `AssertionsWeb`
- step-oriented tracing with `StepTracker`
- screenshots as evidence points
- page objects plus flow orchestration
- a real multi-page public flow:
  - login
  - secure area validation
  - dropdown interaction
  - checkbox state validation
  - add/remove elements example navigation

Default live example values:

- `PEPENIUM_BASE_URL=https://the-internet.herokuapp.com/login`
- `PEPENIUM_WEB_USERNAME=tomsmith`
- `PEPENIUM_WEB_PASSWORD=SuperSecretPassword!`

Run the desktop web showcase directly with:

```text
mvn -pl pepenium-examples -am "-Dpepenium.examples.skip.tests=false" "-Dpepenium.excludedTags=" "-Dtest=ExampleDesktopWebTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

### Android Native Showcase Template

The Android native example is currently positioned as a stronger showcase template rather than a live public-app example.

That Android showcase now emphasizes:

- semantic flow steps through `StepTracker`
- reusable `ActionsApp` and `AssertionsApp`
- explicit page-load boundaries
- screenshots as evidence points
- a short but representative bottom-navigation plus search flow that can be adapted to a real product app

This keeps the Android example honest and useful while avoiding dependence on a third-party public app that may not be stable enough for an official framework showcase.

### iOS Native Showcase Template

The iOS native example follows the same strategy as Android native: it is a stronger showcase template rather than a live public-app example.

That iOS showcase emphasizes:

- semantic flow steps through `StepTracker`
- reusable `ActionsAppIOS` and `AssertionsAppIOS`
- explicit page-load boundaries
- screenshots as evidence points
- a short but representative bottom-navigation plus search flow that can be adapted to a real product app

This keeps the iOS example aligned with the Android example and gives the repository a more coherent native-mobile story.

## Running From the IDE

The intended workflow is:

1. Keep one test per target.
2. Create one IDE run configuration per execution profile you care about.
3. Point those run configurations to the same test class.

Example for the same Android native test:

- `Android Native - Local`
- `Android Native - BrowserStack`
- `Android Native - AWS`

Each run configuration changes only `pepenium.profile`.

That gives you one-click execution without editing the test.

## Local Execution

### Android Native

Default profile for `ANDROID_NATIVE`: `local-android`

Useful environment variables:

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=emulator-5554
ANDROID_DEVICE_NAME=Android Device
APP_PATH=C:\path\to\app.apk
APP_PACKAGE=com.example.app
APP_ACTIVITY=com.example.MainActivity
```

Dockerized Appium with host emulator:

```text
docker compose up -d appium
APPIUM_URL=http://localhost:4723
ANDROID_UDID=host.docker.internal:5555
ANDROID_DEVICE_NAME=Android Emulator
```

Experimental fully dockerized emulator stack:

```text
docker compose -f docker-compose.yaml -f docker-compose.emulator.yaml up -d
APPIUM_URL=http://localhost:4723
ANDROID_UDID=android-emulator:5555
ANDROID_DEVICE_NAME=Android Emulator
```

This emulator mode is intentionally experimental and is best suited to Linux or Windows 11 + WSL2 setups that expose `/dev/kvm`.

### Android Web

Default profile for `ANDROID_WEB`: `local-android-web`

Useful environment variables:

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=emulator-5554
ANDROID_DEVICE_NAME=Android Device
PEPENIUM_BASE_URL=https://example.com
```

### Desktop Web

Default profile for `WEB_DESKTOP`: `local-web`

Useful environment variables:

```text
PEPENIUM_BASE_URL=https://the-internet.herokuapp.com/login
PEPENIUM_WEB_USERNAME=tomsmith
PEPENIUM_WEB_PASSWORD=SuperSecretPassword!
```

## BrowserStack and AWS

BrowserStack profiles are backed by the YAML example files under:

- `pepenium-core/src/main/resources/browserstackExamples/browserstackAndroid.yml.example`
- `pepenium-core/src/main/resources/browserstackExamples/browserstackAndroidWEB.yml.example`
- `pepenium-core/src/main/resources/browserstackExamples/browserstackIOS.yml.example`
- `pepenium-core/src/main/resources/browserstackExamples/browserstackIOSWEB.yml.example`
- `pepenium-core/src/main/resources/browserstackExamples/browserstack.yml.example`

AWS Device Farm profiles follow the same `TestTarget` model, but are still geared toward packaged execution flows defined in `pom.xml`.

The execution profile catalog itself is now externalized in `pepenium-core/src/main/resources/execution-profiles.yml`, so available profile ids and descriptions are visible without reading Java code.

## Screenshots, Logging and Failure Diagnostics

Pepenium includes:

- `takeScreenshot()` for safer evidence capture
- `takeScreenshotFast()` for lighter checkpoints
- temp-directory fallback when `DEVICEFARM_SCREENSHOT_PATH` is not set
- a Pepenium ASCII banner when a session starts
- compact logs with profile, target, driver and short session id
- automatic failure reporting with screenshot path and runtime context
- recent step tracking in failure summaries

Automatic failure context includes:

- profile, target, driver and session id
- web URL and title for web sessions
- package, activity or context details for mobile sessions when available
- a summarized capabilities view instead of noisy raw capability dumps
- the last recorded framework steps before the failure

Step tracking behavior:

- records common `Actions*` operations automatically
- keeps only the last `10` steps by default
- can be tuned with `PEPENIUM_STEP_TRACKER_LIMIT` or `-Dpepenium.step.tracker.limit=...`
- can be enriched manually from tests or flows with `step("Open search")`

If you need extra framework detail, enable:

```text
PEPENIUM_DETAIL_LOGGING=true
```

or:

```text
-Dpepenium.detail.logging=true
```

## Current Status

Pepenium is already useful for real automation work. It has been exercised against real Android app flows, local emulators, remote configuration paths and reusable action layers. The next major direction is to keep improving reusable-library readiness and higher-level diagnostics.

## Documentation

- English quick start: [QUICK-START.md](QUICK-START.md)
- Spanish quick start: [QUICK-START.es.md](QUICK-START.es.md)
- Spanish README: [README.es.md](README.es.md)
- Environment reference: [ENVIRONMENT.md](ENVIRONMENT.md)

