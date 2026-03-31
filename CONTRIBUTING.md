# Contributing to Pepenium

First of all, thank you for taking the time to contribute to **Pepenium**.
Contributions of any kind are welcome: code, documentation, bug reports, or suggestions.

---

## Project Language

To keep the project consistent and accessible:

- **Issues and pull requests:** English
- **Documentation:** English, with Spanish mirrors where applicable
- **Code and comments:** English

Please stick to English for all new contributions.

---

## Reporting Bugs

Before opening a bug report:

1. Check existing issues to avoid duplicates.
2. Make sure you are using the latest version of the project.

When opening a bug report, please use the bug-report issue template and include:

- a clear description of the problem
- steps to reproduce
- expected vs actual behavior
- relevant logs or stack traces
- environment details such as OS, Java version and provider

---

## Requesting Features

Feature requests are welcome.
Please use the feature-request issue template and clearly describe:

- the problem you are trying to solve
- the proposed solution
- why it would be useful for the project

Well-defined feature requests are much easier to evaluate and prioritize.

---

## Running the Project Locally

Basic requirements:

- Java 11
- Maven
- Appium, when working with mobile automation

Typical commands:

```bash
mvn verify
```

Some features require specific execution profiles or provider credentials, especially BrowserStack and AWS Device Farm flows.

When validating public API compatibility, `mvn verify` now runs the automatic binary/source compatibility check for the documented public API. Also run:

```bash
mvn -q -pl pepenium-core,pepenium-toolkit -am install -DskipTests
mvn -q -U -f consumer-smoke/pom.xml clean test-compile
```

---

## Pull Requests

Before submitting a pull request:

- ensure the project builds successfully
- keep changes focused and scoped
- update documentation if behavior changes
- update `CHANGELOG.md` when the change is notable
- run the standalone consumer smoke when changing documented public API
- prefer a deprecation-first path for documented public API instead of removing or renaming it immediately
- avoid breaking changes unless clearly justified
- if you change `BaseTest`, `TestTarget`, built-in execution profile ids or reporting-contract expectations, update `docs/API.md` and the contract-focused tests together

Pull requests should:

- reference an existing issue when possible
- include a clear description of what changed and why

---

## Project Structure

High-level structure:

- `pepenium-core/` - framework runtime, execution model and provider/config infrastructure
- `pepenium-toolkit/` - reusable test-author helpers such as actions and assertions
- `pepenium-examples/` - repository-only showcase tests, flows and page objects

Please respect the existing module boundaries and avoid adding project-specific logic to `pepenium-core` unless it benefits all users of the framework.

When deciding where to place a change:

- prefer `pepenium-core` for framework lifecycle, execution and provider/config infrastructure
- prefer `pepenium-toolkit` for reusable authoring helpers
- prefer `pepenium-examples` for showcase code and templates

`pepenium-examples` should stay focused on runnable examples that consume the framework from inside this repository. It is not a published artifact surface and should not become a second home for reusable framework features.

For public-vs-internal API expectations, see [API.md](docs/API.md).

---

## Versioning and Changelog

This project follows **Semantic Versioning**.

Notable changes should be documented in `CHANGELOG.md` under the **[Unreleased]** section.

Until `1.0.0`, structural refactors are still possible, but changes to documented public API should already be treated carefully.

That now includes the documented `BaseTest` lifecycle model, `TestTarget` defaults, built-in execution profile ids and the current decision that reporting JSON is still evolving rather than versioned public API.

For documented public API, prefer this order whenever practical:

- add a replacement first
- mark the old API with `@Deprecated`
- document the migration path
- remove it only in a later released version unless a correctness or security reason forces a direct break

Before creating a release tag, run the `Release Preflight` GitHub Actions workflow, or the equivalent local validation, so version alignment, `CHANGELOG.md`, `verify`, release-profile packaging and `consumer-smoke` are all checked before publication.

---

## Code of Conduct

Be respectful and constructive in discussions.
This project aims to maintain a friendly and professional environment for everyone.

---

Thanks again for contributing to Pepenium.
