param(
    [string] $PepeniumVersion = "",
    [switch] $SkipInstall
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")

function Invoke-Maven {
    param([string[]] $Arguments)

    & mvn @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Maven command failed with exit code ${LASTEXITCODE}: mvn $($Arguments -join ' ')"
    }
}

Push-Location $repoRoot
try {
    if (-not $SkipInstall) {
        Invoke-Maven @(
            "-B",
            "-ntp",
            "-pl",
            "pepenium-core,pepenium-toolkit,pepenium-maven-plugin",
            "-am",
            "install",
            "-DskipTests",
            "-Djacoco.skip=true",
            "-Dcheckstyle.skip=true",
            "-Dspotbugs.skip=true",
            "-Djapicmp.skip=true"
        )
    }

    $smokeArgs = @(
        "-B",
        "-ntp",
        "-U",
        "-f",
        "consumer-smoke/pom.xml",
        "clean",
        "test-compile"
    )

    if (-not [string]::IsNullOrWhiteSpace($PepeniumVersion)) {
        $smokeArgs += "-Dpepenium.version=$PepeniumVersion"
    }

    Invoke-Maven $smokeArgs
} finally {
    Pop-Location
}
