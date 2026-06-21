#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

version="$(mvn -q help:evaluate -Dexpression=project.version -DforceStdout)"
artifacts=(
  "pepenium-core/target/pepenium-${version}.jar"
  "pepenium-toolkit/target/pepenium-toolkit-${version}.jar"
  "pepenium-maven-plugin/target/pepenium-maven-plugin-${version}.jar"
)

build_runtime_artifacts() {
  mvn -B -ntp clean package -DskipTests \
    -Djacoco.skip=true \
    -Dcheckstyle.skip=true \
    -Dspotbugs.skip=true \
    -Djapicmp.skip=true
}

first_hashes="$(mktemp)"
trap 'rm -f "$first_hashes"' EXIT

build_runtime_artifacts
sha256sum "${artifacts[@]}" > "$first_hashes"

build_runtime_artifacts
sha256sum --check "$first_hashes"

echo "Verified reproducible Pepenium runtime artifacts."
