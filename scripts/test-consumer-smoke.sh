#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PEPENIUM_VERSION=""
SKIP_INSTALL=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --version)
      PEPENIUM_VERSION="${2:-}"
      if [[ -z "$PEPENIUM_VERSION" ]]; then
        echo "--version requires a value" >&2
        exit 2
      fi
      shift 2
      ;;
    --skip-install)
      SKIP_INSTALL=1
      shift
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 2
      ;;
  esac
done

cd "$ROOT_DIR"

if [[ "$SKIP_INSTALL" -eq 0 ]]; then
  mvn -B -ntp \
    -pl pepenium-core,pepenium-toolkit,pepenium-maven-plugin \
    -am install \
    -DskipTests \
    -Djacoco.skip=true \
    -Dcheckstyle.skip=true \
    -Dspotbugs.skip=true \
    -Djapicmp.skip=true
fi

SMOKE_ARGS=(-B -ntp -U -f consumer-smoke/pom.xml clean test-compile)
if [[ -n "$PEPENIUM_VERSION" ]]; then
  SMOKE_ARGS+=("-Dpepenium.version=$PEPENIUM_VERSION")
fi

mvn "${SMOKE_ARGS[@]}"
