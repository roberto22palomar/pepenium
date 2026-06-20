# Environment Reference

This document lists the environment variables and system properties currently used by Pepenium.

It is intended to be the single reference point for configuring local runs, remote executions and observability features.

The canonical editor schema is [schema/pepenium.schema.json](schema/pepenium.schema.json). The distributed example
contains a YAML language-server directive, so compatible editors provide completion and report unknown keys before a
test starts.

## Validate before running

Configuration can be validated without opening Selenium/Appium or contacting a provider:

```java
PepeniumConfig.validate(Path.of("pepenium.yml"), "local-web");
```

For Maven projects, declare the versioned plugin once:

```xml
<plugin>
    <groupId>io.github.roberto22palomar</groupId>
    <artifactId>pepenium-maven-plugin</artifactId>
    <version>${pepenium.version}</version>
</plugin>
```

Then validate the selected profile directly:

```text
mvn pepenium:validate-config -Dpepenium.profile=local-web
```

The goal uses `pepenium.yml` in the project root by default. Override it with `-Dpepenium.config=path/to/file.yml`
or skip an intentional validation binding with `-Dpepenium.config.skip=true`.

The lower-level command-line entry point remains available and uses exit code `0` for valid configuration and `2` for
invalid arguments or configuration:

```text
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.classpathScope=test -Dexec.mainClass=io.github.roberto22palomar.pepenium.core.config.PepeniumConfigCli -Dexec.args="--config pepenium.yml --profile local-web"
```

The preflight resolves placeholders for the selected profile and enforces provider ownership rules. It never creates a
driver session.

## Recommended Configuration: `pepenium.yml`

New projects can keep ordinary profile settings in one optional `pepenium.yml` file at the project root. Copy [the complete example](env/pepenium.yml.example) as a starting point.

```yaml
schemaVersion: 1
defaultProfile: local-android

baseUrl: https://example.com

reporting:
  directory: target/pepenium-reports
  screenshotPath: target/pepenium-screenshots

logging:
  detailed: false
  stepLimit: 20

timeouts:
  action: 750ms
  longAction: 30s
  assertion: 10s

capabilities:
  acceptInsecureCerts: false
  vendor:options:
    project: Pepenium
    tags: [smoke, local]

profiles:
  local-android:
    serverUrl: http://127.0.0.1:4723
    device:
      udid: emulator-5554
      name: Android Emulator
    app:
      path: ${APP_BINARY}
    capabilities:
      noReset: false
```

The file is optional. Existing projects using environment variables continue to work unchanged.

Resolution order is:

1. Java system property
2. Environment variable
3. Selected profile in `pepenium.yml`
4. Built-in default

Use `${ENV_VAR}` placeholders for secrets or machine-specific values. Pepenium resolves placeholders only when the selected profile requests that value, and reports the exact YAML path when a referenced variable is missing.

Set `-Dpepenium.config=path/to/config.yml` or `PEPENIUM_CONFIG=path/to/config.yml` to use a different file. An explicitly configured missing file fails early; an absent default `pepenium.yml` is simply ignored.

The YAML surface covers profile selection, local Android connection/app settings, common local Web browser settings, generic capability maps, base URLs, reporting paths, screenshot paths, logging and toolkit timeouts. Existing environment variables remain compatible and continue to override YAML values.

Profile selection still works exactly as before through `-Dpepenium.profile=...` or `PEPENIUM_PROFILE`. BrowserStack keeps using its existing provider-specific YAML files for now; `pepenium.yml` does not replace them.

### YAML schema and structured capabilities

Set `schemaVersion: 1` in new files. Omitted versions remain accepted for compatibility, while unsupported explicit versions fail immediately.

Pepenium validates known configuration sections when the file is loaded. Unknown keys, malformed sections, invalid HTTP(S) URLs, non-positive durations, invalid booleans and non-object `capabilities` fail early with the exact YAML path instead of being silently ignored.

Capability values preserve native YAML types, including booleans, numbers, lists and nested objects. Global capabilities are merged with the selected profile, and profile values win recursively:

```yaml
capabilities:
  vendor:options:
    project: Pepenium
    tags: [regression]

profiles:
  local-android:
    capabilities:
      noReset: false
      settings:
        ignoreUnimportantViews: true
      vendor:options:
        build: ${BUILD_NUMBER}
```

For Appium, unprefixed top-level capability names receive the `appium:` prefix; W3C names and explicitly namespaced keys remain unchanged. Existing `PEPENIUM_WEB_CAPABILITIES` and `PEPENIUM_APPIUM_CAPABILITIES` strings remain supported and override YAML, but structured YAML is recommended for new projects.

### Provider ownership

`pepenium.yml` deliberately has a different responsibility from provider-owned configuration:

| Execution | `pepenium.yml` owns | Provider-owned source |
| --- | --- | --- |
| Local Web/Android | Server URL, device/app/browser settings, capabilities and common settings | None |
| AWS Device Farm | Common settings and additional non-provider capabilities | `DEVICEFARM_*`, `AWS_DEVICE_FARM` and the Device Farm runtime |
| BrowserStack | Common settings and additional non-provider capabilities | Existing BrowserStack YAML for credentials, platforms and `bstack:options` |

For AWS and BrowserStack profiles, Pepenium rejects `serverUrl`, `device`, `app` and `browser` sections in `pepenium.yml` because those values would otherwise be ignored by the provider config. BrowserStack-owned keys such as `bstack:options` and `browserstack.*` are also rejected there with guidance to use the existing BrowserStack YAML.

Common structured capabilities are applied consistently to local, AWS and BrowserStack sessions. Provider-owned platform values are applied afterwards and therefore remain authoritative.

Ready-to-copy examples for common local setups live in [docs/env](env/README.md):

- [`.env.web.example`](env/.env.web.example)
- [`.env.android.local.example`](env/.env.android.local.example)
- [`.env.web.capabilities.example`](env/.env.web.capabilities.example)
- [`.env.android.host-emulator.example`](env/.env.android.host-emulator.example)
- [`.env.android.docker-emulator.example`](env/.env.android.docker-emulator.example)
- [`.env.mobile.capabilities.example`](env/.env.mobile.capabilities.example)

## How To Choose A Config Starting Point

Use this quick rule:

- `WEB_DESKTOP` on your machine: start from [`.env.web.example`](env/.env.web.example)
- `ANDROID_NATIVE` with local Appium: start from [`.env.android.local.example`](env/.env.android.local.example)
- `ANDROID_NATIVE` with Dockerized Appium and host emulator: start from [`.env.android.host-emulator.example`](env/.env.android.host-emulator.example)
- `ANDROID_NATIVE` with Dockerized Appium and Dockerized emulator: start from [`.env.android.docker-emulator.example`](env/.env.android.docker-emulator.example)
- Any Appium-backed mobile profile that needs extra tuning: copy the relevant keys from [`.env.mobile.capabilities.example`](env/.env.mobile.capabilities.example)

## Configuration Precedence In Practice

The precedence order is:

1. Java system property
2. Environment variable
3. Selected `pepenium.yml` profile
4. Built-in default

Practical examples:

- `-Dpepenium.profile=aws-android` wins over `PEPENIUM_PROFILE=local-android`
- if `PEPENIUM_PROFILE` is unset, Pepenium can still choose the target default profile
- if `APPIUM_URL` is unset for local Android, Pepenium falls back to `http://localhost:4723`
- if neither `PEPENIUM_SCREENSHOT_PATH` nor `DEVICEFARM_SCREENSHOT_PATH` is set, screenshots fall back to the Java temporary directory

## Resolution Order

When a setting supports YAML, a Java system property and an environment variable, Pepenium resolves them in this order:

1. Java system property
2. Environment variable
3. Selected `pepenium.yml` profile
4. Built-in default, if one exists

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
- `PEPENIUM_APPIUM_CAPABILITIES`

They are intended for advanced mobile runs when the default Appium option sets need to be tuned without forking the framework.

Notes:

- `PEPENIUM_APPIUM_CAPABILITIES` uses `;` separators and `key=value` entries, for example `appWaitDuration=30000;vendor:flag=true`
- unprefixed keys are applied as `appium:*` capabilities, so `appWaitDuration=30000` becomes `appium:appWaitDuration=30000`
- W3C keys such as `platformName` and `browserName`, and vendor-prefixed keys such as `bstack:options`, are kept as written
- scalar values are typed automatically: `true` / `false` become booleans, integer values become longs and decimal values become doubles
- equivalent Java system properties are supported by lowercasing the variable and replacing `_` with `.`, for example `-Dappium.no.reset=true` or `-Dpepenium.appium.capabilities=appWaitDuration=30000`
## Local Web and Mobile Web

### `PEPENIUM_BASE_URL`

- Required: No
- Default in examples: `https://example.com`
- Used by:
  - desktop web example
  - Android web example
  - iOS web example
- Purpose: Base URL opened by the example tests

### Optional local desktop web capability overrides

These optional environment variables are supported by the built-in local desktop web profiles:

- `local-web`
- `local-web-firefox`
- `local-web-edge`

- `PEPENIUM_WEB_HEADLESS`
- `PEPENIUM_WEB_ACCEPT_INSECURE_CERTS`
- `PEPENIUM_WEB_PAGE_LOAD_STRATEGY`
- `PEPENIUM_WEB_BROWSER_VERSION`
- `PEPENIUM_WEB_BINARY_PATH`
- `PEPENIUM_WEB_ARGS`
- `PEPENIUM_WEB_CAPABILITIES`

Notes:

- `PEPENIUM_WEB_ARGS` uses `;` as separator, for example `--incognito;--window-size=1920,1080`
- `PEPENIUM_WEB_CAPABILITIES` also uses `;` and `key=value` entries, for example `custom:flag=true;custom:retries=3`
- `PEPENIUM_WEB_HEADLESS=true` maps to the browser-specific headless argument for Chromium and Firefox
- `PEPENIUM_WEB_PAGE_LOAD_STRATEGY` accepts `normal`, `eager` or `none`

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

## Toolkit Wait Tuning

These variables and equivalent Java system properties tune Pepenium toolkit waits without changing test code.

### `pepenium.action.timeout.seconds` / `PEPENIUM_ACTION_TIMEOUT_SECONDS`

- Required: No
- Values: positive duration; plain numbers are seconds, and explicit values such as `500ms`, `2s`, `1m` and `PT2S` are also supported
- Default: helper-specific short action timeout
- Purpose: Controls short action waits such as visibility, clickability and quick optional checks

### `pepenium.action.long-timeout.seconds` / `PEPENIUM_ACTION_LONG_TIMEOUT_SECONDS`

- Required: No
- Values: positive duration; plain numbers are seconds, and explicit values such as `500ms`, `2s`, `1m` and `PT2S` are also supported
- Default: helper-specific long action timeout
- Purpose: Controls long action waits such as presence checks and disappearance waits

### `pepenium.assertion.timeout.seconds` / `PEPENIUM_ASSERTION_TIMEOUT_SECONDS`

- Required: No
- Values: positive duration; plain numbers are seconds, and explicit values such as `500ms`, `2s`, `1m` and `PT2S` are also supported
- Default: `6`
- Purpose: Controls waits used by toolkit assertions before they fail

Java system properties win over environment variables. The existing property and environment variable names keep the
`seconds` suffix for compatibility even when an explicit duration unit is used.

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
PEPENIUM_PROFILE=local-web
PEPENIUM_BASE_URL=https://example.com
```

### Local Desktop Web With Driver Tuning

```text
PEPENIUM_PROFILE=local-web
PEPENIUM_BASE_URL=https://example.com
PEPENIUM_WEB_HEADLESS=true
PEPENIUM_WEB_ACCEPT_INSECURE_CERTS=true
PEPENIUM_WEB_PAGE_LOAD_STRATEGY=eager
PEPENIUM_WEB_ARGS=--incognito;--window-size=1920,1080
PEPENIUM_WEB_CAPABILITIES=custom:flag=true;custom:retries=3
```

### Detailed Diagnostics

```text
PEPENIUM_DETAIL_LOGGING=true
PEPENIUM_STEP_TRACKER_LIMIT=20
PEPENIUM_ACTION_TIMEOUT_SECONDS=750ms
PEPENIUM_ASSERTION_TIMEOUT_SECONDS=10
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
PEPENIUM_APPIUM_CAPABILITIES=appWaitDuration=30000;customRetries=3
PEPENIUM_SCREENSHOT_PATH=C:\temp\pepenium-screenshots
```

### Custom Screenshot Directory

```text
PEPENIUM_SCREENSHOT_PATH=C:\temp\pepenium-screenshots
```

