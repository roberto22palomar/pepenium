# Quick Start

This guide shows the fastest path to understand and run Pepenium as it stands in `v0.6.0`.

## 1. Prerequisites

- Java 11
- Maven 3.x
- Appium server for local mobile runs
- An Android emulator or device if you want local Android execution
- BrowserStack or AWS credentials and config for remote execution

## 2. Core Idea

Tests declare a functional target, not a provider-specific config class.

```java
public class ExampleAndroidNativeTest extends BaseTest {

    @Override
    protected TestTarget getTarget() {
        return TestTarget.ANDROID_NATIVE;
    }
}
```

The environment is selected through an execution profile.

## 3. Main Example Tests

- Android native: [ExampleAndroidNativeTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidNativeTest.java)
- Android web: [ExampleAndroidWebTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidWebTest.java)
- iOS native: [ExampleIOSNativeTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSNativeTest.java)
- iOS web: [ExampleIOSWebTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSWebTest.java)
- Desktop web: [ExampleDesktopWebTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/web/ExampleDesktopWebTest.java)

The current web example is a live showcase against `https://the-internet.herokuapp.com/login` and demonstrates:

- real login
- assertions and actions from the toolkit
- step tracking
- screenshots
- navigation across multiple stable public demo pages

The current Android native example is a showcase template rather than a live public-app example. It is intended to demonstrate:

- `ActionsApp`
- `AssertionsApp`
- step-oriented flow design
- page objects with explicit load boundaries
- an Android-friendly test structure ready to adapt to a real app

The current iOS native example follows the same approach. It is intended to demonstrate:

- `ActionsAppIOS`
- `AssertionsAppIOS`
- step-oriented flow design
- page objects with explicit load boundaries
- an iOS-friendly test structure ready to adapt to a real app

## 4. Built-In Execution Profiles

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

These built-in profiles are defined in:

- `pepenium-core/src/main/resources/execution-profiles.yml`

## 5. Run From the IDE

### Android Native

Run [ExampleAndroidNativeTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidNativeTest.java) directly.

Default resolution:

- target: `ANDROID_NATIVE`
- default profile: `local-android`

Useful local environment variables:

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=emulator-5554
ANDROID_DEVICE_NAME=Android Device
APP_PATH=C:\path\to\app.apk
APP_PACKAGE=com.example.app
APP_ACTIVITY=com.example.MainActivity
```

### Desktop Web

Run [ExampleDesktopWebTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/web/ExampleDesktopWebTest.java) directly.

Default resolution:

- target: `WEB_DESKTOP`
- default profile: `local-web`

Optional:

```text
PEPENIUM_BASE_URL=https://the-internet.herokuapp.com/login
PEPENIUM_WEB_USERNAME=tomsmith
PEPENIUM_WEB_PASSWORD=SuperSecretPassword!
```

Run only the desktop web live showcase with:

```text
mvn -pl pepenium-examples -am "-Dpepenium.examples.skip.tests=false" "-Dpepenium.excludedTags=" "-Dtest=ExampleDesktopWebTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

## 6. Run the Same Test Somewhere Else

Override the profile with:

```text
-Dpepenium.profile=browserstack-android
```

or:

```text
PEPENIUM_PROFILE=browserstack-android
```

Examples:

```text
-Dpepenium.profile=aws-android
-Dpepenium.profile=browserstack-ios
-Dpepenium.profile=browserstack-windows-web
```

## 7. Best IDE Workflow

Create multiple run configurations for the same test:

- `Android Native - Local`
- `Android Native - BrowserStack`
- `Android Native - AWS`

Each configuration points to the same class and only changes `pepenium.profile`.

That gives you one-click switching without editing the test.

## 8. What You Will See at Runtime

When a session starts, Pepenium prints an ASCII startup banner and then logs compact execution context.

Typical log context includes:

- execution profile
- target
- driver type
- short session id

## 9. Failure Diagnostics

On test failure, Pepenium automatically reports:

- screenshot path
- execution profile and target
- session id
- web URL and title for web sessions
- package, activity or context details for mobile sessions when available
- the most recent tracked steps before the failure

By default, step tracking:

- records common `Actions*` operations automatically
- keeps the last `10` steps
- can be configured with `PEPENIUM_STEP_TRACKER_LIMIT` or `-Dpepenium.step.tracker.limit=...`

You can also add more human-readable steps from tests or flows with:

```java
step("Accept legal notice");
```

If you want more framework detail, enable:

```text
PEPENIUM_DETAIL_LOGGING=true
```

or:

```text
-Dpepenium.detail.logging=true
```

## 10. Screenshots

Available helpers:

- `takeScreenshot()`
- `takeScreenshotFast()`

`takeScreenshotFast()` is useful for rapid checkpoints where you want lighter capture overhead.

## 11. Where Execution Is Resolved

Core classes involved:

- [BaseTest.java](/C:/dev/workspace/personal/pepenium/pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/runtime/BaseTest.java)
- [TestTarget.java](/C:/dev/workspace/personal/pepenium/pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/TestTarget.java)
- [ExecutionProfiles.java](/C:/dev/workspace/personal/pepenium/pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/ExecutionProfiles.java)
- [ExecutionProfileResolver.java](/C:/dev/workspace/personal/pepenium/pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/ExecutionProfileResolver.java)
- [DefaultDriverSessionFactory.java](/C:/dev/workspace/personal/pepenium/pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/runtime/DefaultDriverSessionFactory.java)
- `pepenium-core/src/main/resources/execution-profiles.yml`

## 12. Suggested First Steps

1. Run `ExampleAndroidNativeTest` locally.
2. Create a second IDE run configuration with `-Dpepenium.profile=browserstack-android`.
3. Run `ExampleDesktopWebTest` locally.
4. Explore `core/configs/...` to see how each provider builds a neutral `DriverRequest`.
5. Explore `toolkit/actions/...` for reusable interaction helpers and `core/config/...` for provider configuration loading.
6. Use [ENVIRONMENT.md](ENVIRONMENT.md) to see all supported environment variables and system properties in one place.

