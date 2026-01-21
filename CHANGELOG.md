# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

### Added


### Changed


### Dependencies


## [0.0.3] - 2026-01-21

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
