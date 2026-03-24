# Quick Start

This guide shows the fastest path to understand and run Pepenium after the execution-model and example-suite refactor.

## 1. Prerequisites

- Java 11
- Maven 3.x
- Appium server for local mobile runs
- An Android emulator or device if you want local Android execution
- BrowserStack or AWS credentials and config for remote execution

## 2. Core Idea

Tests now declare a functional target, not a provider-specific driver config class.

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

- Android native: [ExampleAndroidNativeTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidNativeTest.java)
- Android web: [ExampleAndroidWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidWebTest.java)
- iOS native: [ExampleIOSNativeTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSNativeTest.java)
- iOS web: [ExampleIOSWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSWebTest.java)
- Desktop web: [ExampleDesktopWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/web/ExampleDesktopWebTest.java)

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

## 5. Run From the IDE

### Android Native

Run [ExampleAndroidNativeTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidNativeTest.java) directly.

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

Run [ExampleDesktopWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/web/ExampleDesktopWebTest.java) directly.

Default resolution:

- target: `WEB_DESKTOP`
- default profile: `local-web`

Optional:

```text
PEPENIUM_BASE_URL=https://example.com
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

## 8. Where Execution Is Resolved

Core classes involved:

- [BaseTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/core/BaseTest.java)
- [TestTarget.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/core/TestTarget.java)
- [ExecutionProfiles.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/core/ExecutionProfiles.java)
- [ExecutionProfileResolver.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/core/ExecutionProfileResolver.java)
- [DefaultDriverSessionFactory.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/core/DefaultDriverSessionFactory.java)

## 9. Actions, Assertions and Screenshots

Reusable helpers are available for all supported platforms:

- Web: `ActionsWeb`, `AssertionsWeb`
- Android: `ActionsApp`, `AssertionsApp`
- iOS: `ActionsAppIOS`, `AssertionsAppIOS`

Screenshots support:

- `takeScreenshot()`
- `takeScreenshotFast()`

## 10. Suggested First Steps

1. Run `ExampleAndroidNativeTest` locally.
2. Create a second IDE run configuration with `-Dpepenium.profile=browserstack-android`.
3. Run `ExampleDesktopWebTest` locally.
4. Explore `core/configs/...` to see how each provider builds a neutral `DriverRequest`.
