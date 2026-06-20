# Compatibility Policy

This document defines the compatibility contract Pepenium intends to carry into `1.0.0`.

## Supported Runtime Matrix

| Area | Supported baseline | Continuous validation |
| --- | --- | --- |
| Java bytecode | Java 11 | Compiled with `--release 11` |
| Java runtimes | 11, 17 and 21 | Unit and provider-contract tests on every pull request |
| Build tool | Maven 3.9+ | Enforced by Maven Enforcer |
| Operating systems | Linux, Windows and macOS | Portable tests run across all three in CI |
| Selenium | Managed by the versioned Selenium BOM | Full framework verification |
| Appium Java client | Version pinned by the parent POM | Local, AWS and BrowserStack request-contract tests |
| Configuration | `pepenium.yml` schema version 1 | Runtime validation, CLI preflight and editor schema |

Real device availability, installed browsers, Appium servers and provider accounts remain consumer infrastructure.
Pepenium validates the requests it builds for those environments without requiring secrets in pull-request CI.

## Public API Stability

The classes listed as public in [API.md](API.md) are compatibility checked with `japicmp`. After `1.0.0`:

- patch releases must remain binary and source compatible
- minor releases may add APIs but must not remove or change existing contracts
- incompatible API or configuration-schema changes require a new major version
- built-in execution profile ids remain stable launch identifiers

Provider SDKs and browser/mobile platforms evolve independently. Dependency upgrades are tested against the consumer
smoke project and runtime matrix before release.

## Configuration Compatibility

`schemaVersion: 1` is the supported configuration contract for `1.0.0`. Unknown keys and invalid values fail early.
A future incompatible configuration format will use a new schema version instead of silently changing version 1.

Use [pepenium.schema.json](schema/pepenium.schema.json) for editor completion and static validation. Use
`PepeniumConfig.validate(path, profile)` or `PepeniumConfigCli` to resolve environment placeholders and provider
ownership rules before a test suite opens a driver session.

## Provider Validation Levels

- **Every change:** deterministic request-building and configuration contract tests for local, AWS and BrowserStack.
- **Before release:** full Maven verification, Java 11/17/21 runtime tests, release packaging and external consumer smoke.
- **Consumer/provider environment:** authenticated live-session smoke tests, because credentials, quotas and device
  availability cannot be safely guaranteed in public pull requests.

This separation prevents external provider outages from blocking ordinary contributions while keeping release gates
reproducible.
