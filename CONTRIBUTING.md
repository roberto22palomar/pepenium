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
mvn test
mvn package
```

Some features require specific execution profiles or provider credentials, especially BrowserStack and AWS Device Farm flows.

When validating public API compatibility, also run:

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
- avoid breaking changes unless clearly justified

Pull requests should:

- reference an existing issue when possible
- include a clear description of what changed and why

---

## Project Structure

High-level structure:

- `pepenium-core/` - framework runtime, execution model and provider/config infrastructure
- `pepenium-toolkit/` - reusable test-author helpers such as actions and assertions
- `pepenium-examples/` - showcase tests, flows and page objects

Please respect the existing module boundaries and avoid adding project-specific logic to `pepenium-core` unless it benefits all users of the framework.

When deciding where to place a change:

- prefer `pepenium-core` for framework lifecycle, execution and provider/config infrastructure
- prefer `pepenium-toolkit` for reusable authoring helpers
- prefer `pepenium-examples` for showcase code and templates

For public-vs-internal API expectations, see [API.md](API.md).

---

## Versioning and Changelog

This project follows **Semantic Versioning**.

Notable changes should be documented in `CHANGELOG.md` under the **[Unreleased]** section.

Until `1.0.0`, structural refactors are still possible, but changes to documented public API should already be treated carefully.

---

## Code of Conduct

Be respectful and constructive in discussions.
This project aims to maintain a friendly and professional environment for everyone.

---

Thanks again for contributing to Pepenium.
