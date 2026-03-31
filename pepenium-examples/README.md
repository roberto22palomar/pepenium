# Pepenium Examples

This module is the repository-only examples area for Pepenium.

It exists to show how `pepenium` and `pepenium-toolkit` are intended to be used together through runnable showcase tests, flows and page objects.

## Role

- provide onboarding-friendly examples inside this repository
- demonstrate authoring patterns built on `BaseTest`, `TestTarget`, `Actions*` and `Assertions*`
- keep showcase code separate from the reusable framework artifacts

## Non-goals

- it is not a published Maven consumer artifact
- it is not part of the public API compatibility contract
- it is not where framework internals or reusable library features should live

## Execution Model

Examples are intentionally opt-in.

Their tests stay skipped by default in normal reactor builds so the framework can be built and released without forcing live example execution.

Run the desktop web showcase explicitly with:

```text
mvn -pl pepenium-examples -am "-Dpepenium.examples.skip.tests=false" "-Dpepenium.excludedTags=" "-Dtest=ExampleDesktopWebTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

For the broader project overview and setup guides, start with:

- [`README.md`](../README.md)
- [`docs/START-HERE.md`](../docs/START-HERE.md)
- [`docs/QUICK-START.md`](../docs/QUICK-START.md)
