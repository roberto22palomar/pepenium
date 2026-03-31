param(
    [Parameter(Mandatory = $true)]
    [string]$OutputPath
)

$ErrorActionPreference = "Stop"

$csvPaths = @(
    "pepenium-core/target/site/jacoco/jacoco.csv",
    "pepenium-toolkit/target/site/jacoco/jacoco.csv"
)

$existingCsvPaths = $csvPaths | Where-Object { Test-Path -LiteralPath $_ }

if (-not $existingCsvPaths) {
    throw "No JaCoCo CSV files found. Expected one of: $($csvPaths -join ', ')"
}

$lineMissed = 0
$lineCovered = 0

foreach ($csvPath in $existingCsvPaths) {
    foreach ($row in Import-Csv -LiteralPath $csvPath) {
        $lineMissed += [int]$row.LINE_MISSED
        $lineCovered += [int]$row.LINE_COVERED
    }
}

$totalLines = $lineMissed + $lineCovered
if ($totalLines -le 0) {
    throw "Unable to compute coverage because no line counters were found."
}

$coverageRatio = $lineCovered / $totalLines
$coveragePercent = [Math]::Round($coverageRatio * 100, 1)

$color = if ($coveragePercent -ge 80) {
    "brightgreen"
} elseif ($coveragePercent -ge 70) {
    "green"
} elseif ($coveragePercent -ge 60) {
    "yellowgreen"
} elseif ($coveragePercent -ge 50) {
    "yellow"
} else {
    "orange"
}

$outputDir = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDir)) {
    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
}

$badge = [ordered]@{
    schemaVersion = 1
    label = "coverage"
    message = "$coveragePercent%"
    color = $color
}

$badge | ConvertTo-Json -Compress | Set-Content -LiteralPath $OutputPath -Encoding UTF8

Write-Host "Coverage badge written to $OutputPath ($coveragePercent%)."
