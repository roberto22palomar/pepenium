# Security Policy

## Supported Versions

Pepenium is currently maintained on the latest released version and the default development branch.

| Version | Supported |
| --- | --- |
| Latest `0.9.x` release | Yes |
| `main` | Yes |
| `< 0.9.0` | No |

Older `0.9.x` releases may receive guidance, but fixes are delivered in the latest patch release. Consumers should upgrade to the newest available `0.9.x` version before reporting an issue that may already be resolved.

## Reporting a Vulnerability

Please do not open a public GitHub issue for security-sensitive reports.

Instead, report potential vulnerabilities privately through one of these channels:

- [Open a private GitHub Security Advisory](https://github.com/roberto22palomar/pepenium/security/advisories/new)
- If private advisories are unavailable, use a private contact method listed on the [maintainer's GitHub profile](https://github.com/roberto22palomar) and do not include sensitive details in a public issue

When reporting, please include:

- A clear description of the issue
- The affected version, branch or workflow
- Reproduction steps or a minimal proof of concept
- The potential impact
- Any suggested mitigation if you already have one

## Response Expectations

Best effort targets:

- Initial acknowledgment within 5 business days
- Triage and next-step guidance after review
- A coordinated fix and disclosure approach when the report is confirmed

## Scope

This policy applies to:

- Framework runtime behavior
- Configuration loading and execution profiles
- CI/CD workflows and packaging
- Dependency-related security concerns

Reports that are purely support requests, feature requests or local environment setup issues may be handled through the normal repository channels instead.
