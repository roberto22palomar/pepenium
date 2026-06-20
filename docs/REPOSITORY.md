# Repository Map

This page explains where things belong in Pepenium so the repository stays easy to scan as it grows.

## Root Directory

The root should stay focused on files a new contributor expects immediately:

- `README.md` / `README.es.md`: project entry points
- `pom.xml`: Maven reactor and shared build lifecycle
- `CHANGELOG.md`: release history
- `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, `LICENSE`: open source project policy
- `docker-compose.yaml` and `docker-compose.emulator.yaml`: local Appium and emulator launch helpers

Avoid adding new one-off setup files to the root. Prefer one of the grouped directories below.

## Main Modules

| Path | Purpose |
| --- | --- |
| `pepenium-core/` | Runtime, execution profiles, session lifecycle, provider configuration and JUnit integration |
| `pepenium-toolkit/` | User-facing actions, assertions, locator helpers and reusable authoring contracts |
| `pepenium-examples/` | Repository-only runnable showcases and templates built on top of the framework |
| `consumer-smoke/` | Standalone Maven consumer used to prove public API consumption outside the reactor |

Keep framework features in `pepenium-core` or `pepenium-toolkit`, not in `pepenium-examples`.

## Supporting Directories

| Path | Purpose |
| --- | --- |
| `docs/` | User guides, API policy, reporting docs and repository documentation |
| `docs/env/` | Copyable environment templates for common local/provider setups |
| `docs/es/` | Spanish mirrors for user-facing guides when available |
| `docs/assets/` | Documentation images and diagrams |
| `config/` | Repository-level build/configuration files that should not clutter the root |
| `scripts/` | Local helper commands for repeatable contributor workflows |
| `.github/` | GitHub Actions workflows, issue templates and repository automation |

Generated local output should stay out of source control. The root `.gitignore` already covers `target/`, `tmp/`, `artifacts/`, `logs/`, local `*.env` files and local `*.log` files.

## Placement Rules

- Put public test authoring APIs in `pepenium-toolkit` unless they are core lifecycle or execution concepts.
- Put provider/session/runtime wiring in `pepenium-core`.
- Put runnable examples and templates in `pepenium-examples`.
- Put external-consumer compatibility checks in `consumer-smoke`.
- Put consumer adaptation guidance in `docs/ADAPTING.md` and prove extension APIs from `consumer-smoke`.
- Put environment templates in `docs/env`.
- Put build-tool configuration in `config`.
- Put repeatable local helper commands in `scripts`.
- Update [API.md](API.md) when a change affects the documented public API surface.
- Update [ENVIRONMENT.md](ENVIRONMENT.md) when a change adds or changes a supported environment variable or system property.
- Update [REPORTING.md](REPORTING.md) when a change affects report files, report paths or report behavior.
