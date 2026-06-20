# Consumer Smoke

This project is intentionally outside the main Maven reactor.

Its purpose is to validate that Pepenium can be consumed from a separate project using the documented public API, instead of only compiling inside the framework repository itself.

It intentionally declares `pepenium-toolkit` as its only direct Pepenium dependency and uses it with `test` scope. Core runtime imports in the smoke sources therefore prove that `pepenium-toolkit` provides `pepenium` transitively, matching the recommended consumer setup.

## Typical Validation Flow

Use the repository helper script from the root:

```powershell
.\scripts\Test-ConsumerSmoke.ps1
```

On Bash-compatible shells:

```bash
./scripts/test-consumer-smoke.sh
```

If needed, the consumed version can be overridden:

```powershell
.\scripts\Test-ConsumerSmoke.ps1 -PepeniumVersion 0.9.7
```

```bash
./scripts/test-consumer-smoke.sh --version 0.9.7
```

## What It Covers

The smoke sources intentionally model the expected user-facing workflow:

- extending `BaseTest`
- or using `@PepeniumTest` with `@PepeniumInject`
- using `@PepeniumPage` page objects initialized through Selenium `PageFactory`
- declaring a `TestTarget`
- building page objects and flows with `Actions*`
- compiling against shared `WebActions`, `MobileActions`, `WebAssertions` and `MobileAssertions` contracts
- compiling against `PepeniumBy` mobile locators for Android/iOS-compatible page objects
- reaching core runtime APIs transitively through the single `pepenium-toolkit` dependency
- contributing a consumer-owned `team-grid-web` profile through `ExecutionProfileProvider` and `META-INF/services`
- using `Assertions*`
- enriching steps with `step("...")` or `PepeniumSteps`

The tests are designed for compilation validation, not live execution.

This same flow is intended to run in CI so public API consumption stays protected over time.
