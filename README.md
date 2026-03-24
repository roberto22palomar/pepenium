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

Pepenium is a Java automation framework for Android, iOS and Web, built on top of Appium, Selenium and JUnit 5.

Its current direction is simple to use, profile-driven execution: tests declare what they are, and execution profiles decide where they run.

## Why Pepenium

- One test per functional target, not one test per infrastructure provider
- Shared execution model for local, BrowserStack and AWS Device Farm
- Reusable `Actions*` and `Assertions*` helpers for Web, Android and iOS
- Centralized driver/session lifecycle through a single session factory
- Screenshot helpers designed for fast flows without blurred captures

See [QUICK-START.md](QUICK-START.md) for the fastest way to run it and [CHANGELOG.md](CHANGELOG.md) for release history.

## What Changed Up To v0.5.0

- Unified session creation around `DriverRequest`, `DriverSession` and `DefaultDriverSessionFactory`
- Introduced `TestTarget` and execution profiles so tests no longer return provider-specific config classes
- Simplified examples to one test per functional target
- Added reusable assertions for Web, Android and iOS
- Improved screenshot settling and added `takeScreenshotFast()`
- Expanded BrowserStack execution support across the YAML-defined device sets

## Current Architecture

### `core`

Framework-level runtime and execution pieces:

- `BaseTest`
- `DriverConfig`
- `DriverRequest`
- `DriverSession`
- `DriverSessionFactory`
- `DefaultDriverSessionFactory`
- `ExecutionProfile`
- `ExecutionProfiles`
- `ExecutionProfileResolver`
- `TestTarget`

Provider-specific request builders currently live under:

- `core/configs/local`
- `core/configs/browserstack`
- `core/configs/aws`

### `toolkit`

Reusable building blocks:

- Web: `ActionsWeb`, `AssertionsWeb`
- Android: `ActionsApp`, `AssertionsApp`
- iOS: `ActionsAppIOS`, `AssertionsAppIOS`
- common utility classes such as YAML loaders and BrowserStack config mappers
- example page objects and flows under `toolkit/myProjectExample`

### `tests`

Example tests showing the intended usage pattern:

- `tests/myProjectExample/android`
- `tests/myProjectExample/ios`
- `tests/myProjectExample/web`

Examples are now grouped by functional target instead of by environment.

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

## Example Tests

- Android native: [ExampleAndroidNativeTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidNativeTest.java)
- Android web: [ExampleAndroidWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidWebTest.java)
- iOS native: [ExampleIOSNativeTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSNativeTest.java)
- iOS web: [ExampleIOSWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSWebTest.java)
- Desktop web: [ExampleDesktopWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/web/ExampleDesktopWebTest.java)

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
PEPENIUM_BASE_URL=https://example.com
```

## BrowserStack and AWS

BrowserStack profiles are backed by the YAML example files under:

- `src/test/resources/browserstackExamples/browserstackAndroid.yml.example`
- `src/test/resources/browserstackExamples/browserstackAndroidWEB.yml.example`
- `src/test/resources/browserstackExamples/browserstackIOS.yml.example`
- `src/test/resources/browserstackExamples/browserstackIOSWEB.yml.example`
- `src/test/resources/browserstackExamples/browserstack.yml.example`

AWS Device Farm profiles follow the same `TestTarget` model, but are still geared toward packaged execution flows defined in `pom.xml`.

## Actions, Assertions and Screenshots

Pepenium includes platform-specific actions and assertions:

- Web: `ActionsWeb`, `AssertionsWeb`
- Android: `ActionsApp`, `AssertionsApp`
- iOS: `ActionsAppIOS`, `AssertionsAppIOS`

Recent screenshot improvements:

- bounded settling before capture
- `takeScreenshotFast()` for lighter checkpoints
- temp-directory fallback when `DEVICEFARM_SCREENSHOT_PATH` is not set
- better behavior in fast flows, especially on mobile

## Current Status

Pepenium is already useful for real automation work. It has been exercised against real Android app flows, remote configuration paths and reusable actions/assertions. The next major direction is to keep improving execution ergonomics and reusable-library readiness.

## Documentation

- English quick start: [QUICK-START.md](QUICK-START.md)
- Spanish quick start: [QUICK-START.es.md](QUICK-START.es.md)
- Spanish README: [README.es.md](README.es.md)
