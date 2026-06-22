# Project History

Pepenium started as a practical Appium/Selenium automation project and has gradually moved toward a reusable open-source testing framework.

This page gives contributors a quick historical map: what each phase changed, why it mattered, and what the project is trying to protect now.

## Timeline

| Line | Focus | Why it mattered |
| --- | --- | --- |
| `0.1.x` | Initial Android, iOS and Web automation support | Established the basic Appium/Selenium/JUnit foundation. |
| `0.2.x` | English public action API | Made the authoring surface more accessible to external users. |
| `0.3.x` | Dependency modernization and CI polish | Brought Selenium, Appium, SLF4J, Log4j and Maven plugins up to date. |
| `0.4.x` | Screenshot reliability | Replaced slow or blurry capture waits with bounded settle helpers and fast screenshot paths. |
| `0.5.x` | Target/profile execution model | Decoupled tests from provider-specific config classes through `TestTarget`, execution profiles and session factories. |
| `0.6.x` | Real Maven modules | Split runtime, toolkit and examples into `pepenium-core`, `pepenium-toolkit` and `pepenium-examples`. |
| `0.7.x` | Reusable toolkit and consumer validation | Added assertions, action logging, public API docs, `consumer-smoke`, Dockerized Appium guidance and stronger examples. |
| `0.8.x` | Library-quality build gates | Added Enforcer, JaCoCo, Checkstyle, SpotBugs, release metadata and Maven Central readiness. |
| `0.9.0` | Native reporting | Added per-test HTML/JSON reports, suite index, timeline diagnostics and screenshot previews. |
| `0.9.3` | Compatibility discipline | Added `japicmp`, release preflight, typed execution-profile registry and a clearer public API policy. |
| `0.9.4` | Annotation-first authoring | Added `@PepeniumTest`, `@PepeniumInject`, `@PepeniumPage` and `PepeniumSteps` as the recommended low-boilerplate path. |
| `0.9.7` | Configuration and report polish | Improved environment examples, screenshot path overrides, capability logging and report readability. |
| Unreleased | Open-source hardening and cross-platform contracts | Focuses on clearer public contracts, Android/iOS consistency, safer configuration, repository hygiene and repeatable contributor workflows. |

## Current Direction

The project is now converging on a simple promise:

- tests declare the functional target they need
- execution profiles decide where that target runs
- Pepenium owns driver/session lifecycle, diagnostics and reporting
- toolkit contracts keep authoring portable across Web, Android and iOS where practical
- examples and consumer smoke prove the framework can be learned and consumed from outside the main reactor

## What Should Stay Stable

These areas are now treated as the most important compatibility anchors:

- `TestTarget` values and built-in execution profile ids
- annotation-first authoring with `@PepeniumTest`, `@PepeniumInject`, `@PepeniumPage` and `PepeniumSteps`
- classic `BaseTest` authoring for teams that prefer inheritance
- toolkit authoring contracts such as `WebActions`, `MobileActions`, `WebAssertions` and `MobileAssertions`
- `PepeniumBy` locators for Android/iOS-compatible page objects
- the native HTML report experience as a user-facing diagnostics feature

Reporting JSON remains useful but evolving; it should not be treated as a stable public schema until explicit schema versioning exists.

## Open Source Shape

Recent repository work is intentionally about making Pepenium easier to approach:

- root files are reserved for project entry points, policy and launch helpers
- environment templates live under `docs/env/`
- build configuration lives under `config/`
- repeatable contributor commands live under `scripts/`
- public API consumption is validated by `consumer-smoke`
- repository placement rules live in [REPOSITORY.md](REPOSITORY.md)

The result should be a project that is easier to inspect, easier to run, and harder to accidentally turn into a private one-off automation folder.

## Next Historical Milestone

The next useful milestone is `1.0.0`.

Before that, Pepenium should keep tightening:

- the documented public API surface
- the consumer-smoke coverage for real external usage
- the demo path from clone to first meaningful report
- compatibility expectations for minor releases
- reporting boundaries between supported HTML diagnostics and evolving JSON artifacts
