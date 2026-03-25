# Environment Reference

This document lists the environment variables and system properties currently used by Pepenium.

It is intended to be the single reference point for configuring local runs, remote executions and observability features.

## Resolution Order

When a setting supports both a Java system property and an environment variable, Pepenium resolves them in this order:

1. Java system property
2. Environment variable
3. Built-in default, if one exists

## Execution Selection

### `pepenium.profile`

- Type: Java system property
- Required: No
- Purpose: Selects the execution profile to use for the current run
- Example:

```text
-Dpepenium.profile=browserstack-android
```

### `PEPENIUM_PROFILE`

- Type: Environment variable
- Required: No
- Purpose: Alternative way to select the execution profile for the current run
- Example:

```text
PEPENIUM_PROFILE=local-web
```

## Local Android

### `APPIUM_URL`

- Required: No
- Default: `http://localhost:4723`
- Used by:
  - local Android native
  - local Android web
- Purpose: Appium server URL for local mobile execution

### `ANDROID_UDID`

- Required: No
- Default: `emulator-5554`
- Used by:
  - local Android native
  - local Android web
- Purpose: Target Android emulator or device identifier

### `ANDROID_DEVICE_NAME`

- Required: No
- Default: `Android Device`
- Used by:
  - local Android native
  - local Android web
- Purpose: Human-readable device name passed into capabilities

### `APP_PATH`

- Required: No
- Used by:
  - local Android native
- Purpose: Path to the APK or app artifact to install locally

### `APP_PACKAGE`

- Required: No
- Used by:
  - local Android native
- Purpose: Android app package to launch when no `APP_PATH` is provided

### `APP_ACTIVITY`

- Required: No
- Used by:
  - local Android native
- Purpose: Android activity to launch together with `APP_PACKAGE`

## Local Web and Mobile Web

### `PEPENIUM_BASE_URL`

- Required: No
- Default in examples: `https://example.com`
- Used by:
  - desktop web example
  - Android web example
  - iOS web example
- Purpose: Base URL opened by the example tests

## AWS Device Farm

### `DEVICEFARM_DEVICE_NAME`

- Required: Context-dependent
- Used by:
  - AWS Android native
  - AWS Android web
  - AWS iOS native
- Purpose: Device name provided by the AWS Device Farm execution environment

### `DEVICEFARM_APP_PATH`

- Required: Context-dependent
- Used by:
  - AWS Android native
  - AWS iOS native
- Purpose: App artifact path provided inside the AWS Device Farm environment

### `AWS_DEVICE_FARM`

- Required: No
- Used by:
  - AWS Android native detection
  - AWS Android web detection
  - AWS iOS native detection
- Purpose: Signals that the run is happening inside AWS Device Farm

### `IOS_APP_PATH`

- Required: No
- Used by:
  - AWS iOS native
- Purpose: Fallback iOS app path when `DEVICEFARM_APP_PATH` is not available

## Screenshot Output

### `DEVICEFARM_SCREENSHOT_PATH`

- Required: No
- Default fallback: Java temporary directory (`java.io.tmpdir`)
- Used by:
  - web screenshots
  - Android screenshots
  - iOS screenshots
  - automatic failure screenshots
- Purpose: Base directory where screenshots are written

## Observability

### `pepenium.detail.logging`

- Type: Java system property
- Required: No
- Default: `false`
- Purpose: Enables additional framework detail logging such as internal stacktraces
- Example:

```text
-Dpepenium.detail.logging=true
```

### `PEPENIUM_DETAIL_LOGGING`

- Type: Environment variable
- Required: No
- Default: `false`
- Purpose: Environment-variable alternative to enable detailed framework logging
- Example:

```text
PEPENIUM_DETAIL_LOGGING=true
```

### `pepenium.step.tracker.limit`

- Type: Java system property
- Required: No
- Default: `10`
- Purpose: Controls how many recent steps are kept in step tracking history
- Example:

```text
-Dpepenium.step.tracker.limit=20
```

### `PEPENIUM_STEP_TRACKER_LIMIT`

- Type: Environment variable
- Required: No
- Default: `10`
- Purpose: Environment-variable alternative to configure tracked-step history length
- Example:

```text
PEPENIUM_STEP_TRACKER_LIMIT=20
```

## BrowserStack Notes

BrowserStack execution in the current codebase is primarily configured through YAML files rather than environment variables.

Main files:

- `pepenium-examples/src/test/resources/browserstack.yml`
- `pepenium-examples/src/test/resources/browserstackMac.yml`
- `pepenium-examples/src/test/resources/browserstackAndroid.yml`
- `pepenium-examples/src/test/resources/browserstackAndroidWEB.yml`
- `pepenium-examples/src/test/resources/browserstackIOS.yml`
- `pepenium-examples/src/test/resources/browserstackIOSWEB.yml`

Those YAML files define items such as:

- username
- access key
- app id
- platforms
- browserstack local flag

## Practical Setup Examples

### Local Android Native

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=emulator-5554
ANDROID_DEVICE_NAME=Android Device
APP_PATH=C:\apps\my-app.apk
```

### Local Desktop Web

```text
PEPENIUM_BASE_URL=https://example.com
```

### Detailed Diagnostics

```text
PEPENIUM_DETAIL_LOGGING=true
PEPENIUM_STEP_TRACKER_LIMIT=20
```

