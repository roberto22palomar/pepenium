param(
    [Parameter(Mandatory = $true)]
    [string]$ReleaseVersion
)

$ErrorActionPreference = "Stop"

$normalizedVersion = $ReleaseVersion -replace '^v', ''

if ($normalizedVersion -notmatch '^\d+\.\d+\.\d+$') {
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

$changelogPattern = '^## \[' + [regex]::Escape($normalizedVersion) + '\]( - \d{4}-\d{2}-\d{2})?$'
$changelogLines = Get-Content -LiteralPath "CHANGELOG.md"

if (-not ($changelogLines | Where-Object { $_ -match $changelogPattern })) {
    throw "CHANGELOG.md must contain a release heading for [$normalizedVersion] before publishing."
}

if (-not ($changelogLines | Where-Object { $_ -eq "## [Unreleased]" })) {
    throw "CHANGELOG.md must keep the [Unreleased] section."
}

if ($env:GITHUB_OUTPUT) {
    Add-Content -LiteralPath $env:GITHUB_OUTPUT -Value "normalized_version=$normalizedVersion"
}

Write-Host "Validated release metadata for version $normalizedVersion."
