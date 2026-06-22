param(
    [Parameter(Mandatory = $true)]
    [string]$ReleaseVersion
)

$ErrorActionPreference = "Stop"

$normalizedVersion = $ReleaseVersion -replace '^v', ''

if ($normalizedVersion -notmatch '^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$') {
    throw "Release version '$ReleaseVersion' must use semantic version format like 1.2.3 or v1.2.3."
}

[xml]$pom = Get-Content -LiteralPath "pom.xml"
$projectVersion = $pom.project.version

if ([string]::IsNullOrWhiteSpace($projectVersion)) {
    throw "Unable to resolve <version> from pom.xml."
}

if ($projectVersion -ne $normalizedVersion) {
    throw "pom.xml version '$projectVersion' does not match requested release version '$normalizedVersion'."
}

$modulePoms = @(
    "pepenium-core/pom.xml",
    "pepenium-toolkit/pom.xml",
    "pepenium-maven-plugin/pom.xml",
    "pepenium-examples/pom.xml"
)
foreach ($modulePom in $modulePoms) {
    [xml]$module = Get-Content -LiteralPath $modulePom
    $parentVersion = $module.project.parent.version
    if ($parentVersion -ne $normalizedVersion) {
        throw "$modulePom parent version '$parentVersion' does not match release version '$normalizedVersion'."
    }
}

if ($normalizedVersion -match '-') {
    throw "Release version '$normalizedVersion' must not be a snapshot or prerelease version."
}

$changelogPattern = '^## \[' + [regex]::Escape($normalizedVersion) + '\] - (\d{4}-\d{2}-\d{2})$'
$changelogLines = Get-Content -LiteralPath "CHANGELOG.md"
$releaseHeading = $changelogLines | Where-Object { $_ -match $changelogPattern } | Select-Object -First 1

if (-not $releaseHeading) {
    throw "CHANGELOG.md must contain a dated release heading like '## [$normalizedVersion] - YYYY-MM-DD' before publishing."
}

$null = $releaseHeading -match $changelogPattern
$releaseDate = [DateTime]::ParseExact($Matches[1], "yyyy-MM-dd", [Globalization.CultureInfo]::InvariantCulture)
$outputTimestampValue = $pom.project.properties.'project.build.outputTimestamp'
if ([string]::IsNullOrWhiteSpace($outputTimestampValue)) {
    throw "pom.xml must define project.build.outputTimestamp for reproducible release artifacts."
}
try {
    $outputTimestamp = [DateTimeOffset]::Parse($outputTimestampValue, [Globalization.CultureInfo]::InvariantCulture)
} catch {
    throw "project.build.outputTimestamp '$outputTimestampValue' must be a valid ISO-8601 timestamp."
}
if ($outputTimestamp.UtcDateTime.Date -ne $releaseDate.Date) {
    throw "project.build.outputTimestamp date '$($outputTimestamp.UtcDateTime.ToString('yyyy-MM-dd'))' must match changelog release date '$($releaseDate.ToString('yyyy-MM-dd'))'."
}

if (-not ($changelogLines | Where-Object { $_ -eq "## [Unreleased]" })) {
    throw "CHANGELOG.md must keep the [Unreleased] section."
}

if ($env:GITHUB_OUTPUT) {
    Add-Content -LiteralPath $env:GITHUB_OUTPUT -Value "normalized_version=$normalizedVersion"
}

Write-Host "Validated release metadata for version $normalizedVersion."
