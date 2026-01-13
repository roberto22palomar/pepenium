# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

## [0.2.0] - 2026-01-13

### Changed
- **Breaking change**: Migrated Web, Android and iOS Actions APIs to English.
  - Renamed public methods
  - Updated comments and logs
  - No behavior changes intended

### Security
- Planned updates for vulnerable Maven dependencies.

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
