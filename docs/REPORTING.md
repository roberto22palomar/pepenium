# Reporting

Pepenium now ships with a native reporting flow that works out of the box after test execution.

By default, reports are written under:

```text
target/pepenium-reports/
```

Generated artifacts include:

- `index.html`: suite-level entry point with summary cards, profile/provider breakdowns and quick filtering
- `summary.json`: suite-level machine-readable summary
- `report-*.html`: rich per-test HTML reports
- `report-*.json`: per-test machine-readable report payloads
- `screenshots/`: report-linked screenshots when evidence is available

Contract status:

- the HTML reports are supported user-facing diagnostics outputs
- the JSON files are available for integrations and automation, but their schema is still evolving and should not yet be treated as a versioned public API contract
- if Pepenium wants to stabilize reporting JSON later, it should first introduce explicit schema versioning

Per-test HTML reports include:

- execution story and failure story
- diagnostic focus and highlights
- assertion pass/fail badges
- grouped screenshot previews
- timeline events for steps, actions, waits, assertions, screenshots and errors
- runtime context such as target, profile, provider, device, platform and browser details when available

The report directory can be redirected with:

```text
PEPENIUM_REPORT_DIR=/custom/path
```

or:

```text
-Dpepenium.report.dir=/custom/path
```

Console output also prints direct `file:///...` links to the generated per-test report and the report index so local investigation is faster.
