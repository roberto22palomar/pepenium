# Start Here

If this is your first time with Pepenium, do not start by reading the whole repository.

Use this page to get your first success quickly, then dive deeper only when you need more detail.

## Choose Your Goal

| Goal | Recommended path | Time |
| --- | --- | --- |
| Verify the repo builds | Run the compile check | 1-2 min |
| See a real test working quickly | Run the desktop web showcase | 3-5 min |
| Try Android locally with the least friction | Appium in Docker + emulator on host | 5-15 min |
| Experiment with a fully dockerized Android setup | Appium + emulator in Docker | 10-20 min |
| Validate public API consumption from another Maven project | Run consumer smoke | 2-3 min |

## 1. Verify The Repo Builds

From the repository root:

```text
mvn -q -DskipTests test-compile
```

If this passes, the multi-module project is wired correctly on your machine.

## 2. Get A First Real Success Fast

The easiest live example is the desktop web showcase.

From the repository root:

```text
mvn -pl pepenium-examples -am "-Dpepenium.examples.skip.tests=false" "-Dpepenium.excludedTags=" "-Dtest=ExampleDesktopWebTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

This runs a real public flow against `https://the-internet.herokuapp.com/login`.

Use this first if:

- you are new to Pepenium
- you want to verify framework basics before touching mobile setup
- you want the shortest path to a meaningful run

## 3. Recommended Android Local Setup

Recommended path:

- Android emulator on the host
- Appium in Docker

Why this path:

- less local setup than installing Appium manually
- more predictable than a fully dockerized emulator
- closest thing to a practical day-to-day setup

### 3.1 Start Appium

```text
docker compose up -d appium
```

### 3.2 Start Your Host Emulator

Start your Android emulator the way you normally do it on your machine.

### 3.3 Use These Environment Values

Example file: [`.env.android.host-emulator.example`](../.env.android.host-emulator.example)

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=host.docker.internal:5555
ANDROID_DEVICE_NAME=Android Emulator
```

### 3.4 Run Your Android Test

Run your Android-native test with the default local profile or from your IDE.

## 4. Experimental Fully Dockerized Android Setup

This is useful for labs, demos and experimentation, but it is not the default recommendation.

Start it with:

```text
docker compose -f docker-compose.yaml -f docker-compose.emulator.yaml up -d
```

Example file: [`.env.android.docker-emulator.example`](../.env.android.docker-emulator.example)

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=android-emulator:5555
ANDROID_DEVICE_NAME=Android Emulator
```

Useful endpoints:

- Appium: `http://localhost:4723/status`
- Emulator noVNC: `http://localhost:6080`

Use this mode only if:

- you explicitly want a fully containerized mobile stack
- your machine supports hardware virtualization well
- you accept that it may be less predictable than a host emulator

## 5. Validate Public API Consumption

If you want to know whether Pepenium is usable from another Maven project, run the consumer smoke.

```text
mvn -q -pl pepenium-core,pepenium-toolkit -am install -DskipTests
mvn -q -U -f consumer-smoke/pom.xml clean test-compile
```

## 6. Check The Native Reports

After a Pepenium-managed execution, the framework now writes a native reporting bundle by default under:

```text
target/pepenium-reports/
```

Start with:

- `target/pepenium-reports/index.html`
- `target/pepenium-reports/summary.json`

The console also prints direct `file:///...` links to the per-test report and the report index so local investigation is faster.

## 7. Where To Go Next

- Use [README.md](../README.md) for the project overview.
- Use [QUICK-START.md](QUICK-START.md) for the fuller walkthrough.
- Use [ENVIRONMENT.md](ENVIRONMENT.md) for every supported variable.
- Use [API.md](API.md) to understand what is public vs internal.
- Use [REPORTING.md](REPORTING.md) for the reporting-specific details.
