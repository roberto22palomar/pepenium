# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

### Added


### Changed


### Dependencies


## [0.5.0] - 2026-03-24

### Added

- Added a unified driver/session layer with `DriverRequest`, `DriverSession`, `DriverSessionFactory` and `DefaultDriverSessionFactory`.
- Added `TestTarget`, execution profiles and execution profile resolution to decouple tests from provider-specific driver config classes.
- Added local execution configs for Android native, Android web and desktop Chrome under `core/configs/local`.
- Added new simplified example tests with one example per functional target:
  - Android native
  - Android web
  - iOS native
  - iOS web
  - desktop web
- Added English and Spanish quick-start guides.
- Added a Pepenium ASCII startup banner when a driver session is created.
- Added structured logging context with execution profile, target, driver type and session id.
- Added automatic failure diagnostics with screenshot path and runtime context for web and mobile executions.
- Added optional detailed framework logging through `PEPENIUM_DETAIL_LOGGING` or `pepenium.detail.logging`.
- Added safe capability summaries for diagnostics instead of logging raw capability payloads.
- Added JUL test logging configuration to suppress noisy Selenium warnings during normal runs.
- Added step tracking with bounded history so failure summaries can show the latest recorded actions.
- Added an external `execution-profiles.yml` catalog for built-in execution profiles.

### Changed

- Refactored `BaseTest` so tests now declare a target instead of returning an environment-specific config class.
- Refactored provider-specific config classes so they now build neutral driver requests instead of creating drivers directly.
- Simplified the `tests/myProjectExample` structure by removing duplication by environment (`local`, `browserstack`, `aws`) and organizing tests by target (`android`, `ios`, `web`).
- Reorganized `toolkit` by responsibility into dedicated `actions`, `browserstack`, `yaml`, `support` and `examples` packages.
- Updated project documentation in English and Spanish to reflect the new execution model, example structure and usage patterns.
- Improved the framework ergonomics for IDE execution by introducing profile-driven environment selection on top of target-based tests.
- Updated Maven project metadata and packaging paths to align with the new example layout and `0.5.0` release.
- Improved screenshot capture behavior for fast flows and introduced `takeScreenshotFast()` for lighter evidence points.
- Improved runtime logging readability with shorter logger names, compact prefixes and cleaner default console output.
- Reduced duplicated framework stacktraces in normal error output while keeping full detail available through the detail-logging switch.
- Trimmed Maven Surefire stacktraces and aligned logging bindings to a clean SLF4J 2.x plus Log4j2 setup.
- Step tracking now keeps only the most recent steps by default and can be configured for longer or shorter histories.
- Execution profile metadata is no longer hardcoded in Java and is now loaded from a visible YAML catalog.

### Dependencies

- Replaced `log4j-slf4j-impl` with `log4j-slf4j2-impl`.
- Added explicit Maven Surefire plugin configuration for cleaner test output.


## [0.4.0] - 2026-03-23

### Added

- Added bounded screenshot settle helpers for mobile and web, including `takeScreenshotFast()` for faster evidence capture when full settling is not needed.

### Changed

- BrowserStack example and configuration flows now execute against all devices defined in the YAML configuration files.
- Improved BrowserStack/device-driven suite lifecycle handling to better support repeated execution across multiple configured targets.
- Reworked screenshot capture waits in Web, Android and iOS actions to avoid blurry captures without blocking tests for several seconds.
- Replaced hardcoded temporary screenshot paths with `DEVICEFARM_SCREENSHOT_PATH` or the platform temporary directory fallback.
- Removed the fixed post-click sleep in web actions and replaced it with a short bounded DOM settle.
- Reduced Android screen-stability polling cost so functional waits remain reliable without penalizing screenshot-heavy flows.
- Cleaned up screenshot-related logs and helper code to reduce noisy diagnostics and dead settle logic.

### Dependencies


## [0.3.0] - 2026-01-21

### Added

- Added README badges for core technologies and CI build status.

### Changed

- docs: translate pom comments to English
- refactor: pom cleanup to improve structure and maintainability
- build: fix pom profile include paths for AWS test JAR packaging
- Adapt YamlLoaders to SnakeYAML 2.x API changes

### Dependencies

- deps: bump org.seleniumhq.selenium:selenium-bom from 4.34.0 to 4.39.0
- deps: bump org.slf4j:slf4j-api from 1.7.32 to 2.0.17
- deps: bump org.apache.logging.log4j:log4j-slf4j-impl from 2.14.1 to 2.25.3
- deps: bump io.appium:java-client from 9.5.0 to 10.0.0
- deps: bump org.apache.maven.plugins:maven-compiler-plugin from 3.10.1 to 3.14.1
- deps: bump maven.dependency.plugin.version from 3.6.1 to 3.9.0
- deps: bump org.projetlombok.lombok from 1.18.38 to 1.18.42
- deps: bump org.yaml:snakeyaml from 1.33 to 2.5

---

## [0.2.0] - 2026-01-13

### Changed

- **Breaking change**: Migrated Web, Android and iOS Actions APIs to English.
  - Renamed public methods
  - Updated comments and logs
  - No behavior changes intended

---

## [0.1.0] - 2026-01-08

### Added

- Initial release of Pepenium.
- Core architecture (`core`, `toolkit`, `tests`) established.
- Support for Android and iOS mobile automation using Appium.
- Web (desktop) automation support using Selenium.
- Execution support for local environments, BrowserStack, and AWS Device Farm.
- Externalized configuration via YAML files.
- Maven-based build and packaging setup.
