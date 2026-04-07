# Environment Reference

This document lists the environment variables and system properties currently used by Pepenium.

It is intended to be the single reference point for configuring local runs, remote executions and observability features.

Ready-to-copy examples for common local setups:

- [`.env.web.example`](../.env.web.example)
- [`.env.android.local.example`](../.env.android.local.example)
- [`.env.android.host-emulator.example`](../.env.android.host-emulator.example)
- [`.env.android.docker-emulator.example`](../.env.android.docker-emulator.example)
- [`.env.mobile.capabilities.example`](../.env.mobile.capabilities.example)

## How To Choose A Config Starting Point

Use this quick rule:

- `WEB_DESKTOP` on your machine: start from [`.env.web.example`](../.env.web.example)
- `ANDROID_NATIVE` with local Appium: start from [`.env.android.local.example`](../.env.android.local.example)
- `ANDROID_NATIVE` with Dockerized Appium and host emulator: start from [`.env.android.host-emulator.example`](../.env.android.host-emulator.example)
- `ANDROID_NATIVE` with Dockerized Appium and Dockerized emulator: start from [`.env.android.docker-emulator.example`](../.env.android.docker-emulator.example)
- Any Appium-backed mobile profile that needs extra tuning: copy the relevant keys from [`.env.mobile.capabilities.example`](../.env.mobile.capabilities.example)

## Configuration Precedence In Practice

The precedence order is:

1. Java system property
2. Environment variable
3. Built-in default

Practical examples:

- `-Dpepenium.profile=aws-android` wins over `PEPENIUM_PROFILE=local-android`
- if `PEPENIUM_PROFILE` is unset, Pepenium can still choose the target default profile
- if `APPIUM_URL` is unset for local Android, Pepenium falls back to `http://localhost:4723`
- if neither `PEPENIUM_SCREENSHOT_PATH` nor `DEVICEFARM_SCREENSHOT_PATH` is set, screenshots fall back to the Java temporary directory

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

Built-in profile ids currently available:

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

## Local Android

### `APPIUM_URL`

- Required: No
- Default: `http://localhost:4723`
- Used by:
  - local Android native
  - local Android web
- Purpose: Appium server URL for local mobile execution
- Docker note: when Appium runs through the repository `docker-compose.yaml`, keep using the published host port such as `http://localhost:4723`

### `ANDROID_UDID`

- Required: No
- Default: `emulator-5554`
- Used by:
  - local Android native
  - local Android web
- Purpose: Target Android emulator or device identifier
- Docker note: when Appium runs in Docker and the emulator stays on the host, this usually becomes a remote ADB address such as `host.docker.internal:5555`
- Docker emulator note: when Appium and the emulator both run in Compose, this can stay as `android-emulator:5555`

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
### Optional Appium capability overrides

These optional environment variables are supported by the Appium-backed built-in profiles:

- local Android native
- local Android web
- AWS Android native
- AWS Android web
- AWS iOS native
- BrowserStack Android native
- BrowserStack Android web
- BrowserStack iOS native
- BrowserStack iOS web

- `APPIUM_PLATFORM_NAME`
- `APPIUM_AUTOMATION_NAME`
- `APPIUM_PLATFORM_VERSION`
- `APPIUM_NEW_COMMAND_TIMEOUT`
- `APPIUM_AUTO_GRANT_PERMISSIONS`
- `APPIUM_NO_RESET`
- `APPIUM_FULL_RESET`
- `APPIUM_DONT_STOP_APP_ON_RESET`
- `APPIUM_SKIP_DEVICE_INITIALIZATION`
- `APPIUM_SKIP_SERVER_INSTALLATION`
- `APPIUM_IGNORE_HIDDEN_API_POLICY_ERROR`
- `APPIUM_AUTO_LAUNCH`
- `APPIUM_ADB_EXEC_TIMEOUT`
- `APPIUM_UIAUTOMATOR2_SERVER_LAUNCH_TIMEOUT`
- `APPIUM_UIAUTOMATOR2_SERVER_INSTALL_TIMEOUT`
- `APPIUM_ANDROID_INSTALL_TIMEOUT`
- `APP_WAIT_PACKAGE`
- `APP_WAIT_ACTIVITY`

They are intended for advanced mobile runs when the default Appium option sets need to be tuned without forking the framework.
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

### `pepenium.screenshot.path`

- Type: Java system property
- Required: No
- Default fallback: Java temporary directory (`java.io.tmpdir`)
- Purpose: Preferred override for the base directory where screenshots are written
- Example:

```text
-Dpepenium.screenshot.path=C:\temp\pepenium-screenshots
```

### `PEPENIUM_SCREENSHOT_PATH`

- Type: Environment variable
- Required: No
- Default fallback: Java temporary directory (`java.io.tmpdir`)
- Used by:
  - web screenshots
  - Android screenshots
  - iOS screenshots
  - automatic failure screenshots
- Purpose: Preferred environment-variable override for the base directory where screenshots are written

### `DEVICEFARM_SCREENSHOT_PATH`

- Type: Environment variable
- Required: No
- Purpose: Legacy compatibility alias for screenshot output, still honored for AWS Device Farm and existing setups
- Recommendation: Prefer `PEPENIUM_SCREENSHOT_PATH` in new local or shared `.env` files

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

### `pepenium.report.dir`

- Type: Java system property
- Required: No
- Default: `target/pepenium-reports`
- Purpose: Overrides the output directory for Pepenium HTML/JSON test reports, suite summary files and generated screenshot previews
- Example:

```text
-Dpepenium.report.dir=C:\reports\pepenium
```

### `PEPENIUM_REPORT_DIR`

- Type: Environment variable
- Required: No
- Default: `target/pepenium-reports`
- Purpose: Environment-variable alternative to override the report output directory and report-owned screenshot/summary artifacts
- Example:

```text
PEPENIUM_REPORT_DIR=C:\reports\pepenium
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

Typical model:

- use `PEPENIUM_PROFILE` or `-Dpepenium.profile` to select the BrowserStack profile
- keep BrowserStack credentials and platform catalogs in YAML
- use environment variables mainly for cross-cutting runtime concerns such as screenshots, detail logging or Appium capability overrides

## Practical Setup Examples

### Local Android Native

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=emulator-5554
ANDROID_DEVICE_NAME=Android Device
APP_PATH=C:\apps\my-app.apk
```

### Local Android Native With Dockerized Appium

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=host.docker.internal:5555
ANDROID_DEVICE_NAME=Android Emulator
APP_PATH=C:\apps\my-app.apk
```

### Local Android Native With Dockerized Appium And Emulator

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=android-emulator:5555
ANDROID_DEVICE_NAME=Android Emulator
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

### Local Android With Extra Appium Tuning

```text
PEPENIUM_PROFILE=local-android
APPIUM_URL=http://127.0.0.1:4723
ANDROID_UDID=emulator-5554
ANDROID_DEVICE_NAME=Android Emulator
APP_PACKAGE=com.example.app
APP_ACTIVITY=.MainActivity
APPIUM_NO_RESET=true
APPIUM_NEW_COMMAND_TIMEOUT=300
PEPENIUM_SCREENSHOT_PATH=C:\temp\pepenium-screenshots
```

### Custom Screenshot Directory

```text
PEPENIUM_SCREENSHOT_PATH=C:\temp\pepenium-screenshots
```

