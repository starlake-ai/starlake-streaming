#!/usr/bin/env bash
# Builds the starlake-streaming release assets for a given version and prepares (or, with
# --run, executes) the `gh release create` command that attaches them to a GitHub Release.
#
# Asset naming must match the pattern resolver in starlake-core's project/Common.scala
# (Resolvers.starlakeStreamingGithubReleases): v<version>/<artifact>-<version>[-<classifier>].<ext>
#
# Usage:
#   scripts/gh-release.sh <version>          # build assets, print the gh release create command
#   scripts/gh-release.sh <version> --run    # build assets and actually run gh release create
#
# Prerequisites: the working tree must already be tagged v<version> (this script does not
# tag or push; that is still done by `sbt release`, see build.sbt's releaseProcess).

set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <version> [--run]" >&2
  exit 1
fi

VERSION="$1"
RUN=false
if [[ "${2:-}" == "--run" ]]; then
  RUN=true
fi

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IVY_MODULE_DIR="$HOME/.ivy2/local/ai.starlake/starlake-streaming_2.13/${VERSION}"
STAGING_DIR="${REPO_ROOT}/target/gh-release/${VERSION}"

echo "==> Building starlake-streaming ${VERSION} and publishing to the local ivy repo"
rm -rf "${IVY_MODULE_DIR}"
(cd "${REPO_ROOT}" && sbt "set ThisBuild / version := \"${VERSION}\"" publishLocal)

echo "==> Staging release assets in ${STAGING_DIR}"
rm -rf "${STAGING_DIR}"
mkdir -p "${STAGING_DIR}"

cp "${IVY_MODULE_DIR}/jars/starlake-streaming_2.13.jar" \
  "${STAGING_DIR}/starlake-streaming_2.13-${VERSION}.jar"
cp "${IVY_MODULE_DIR}/jars/starlake-streaming_2.13-assembly.jar" \
  "${STAGING_DIR}/starlake-streaming_2.13-${VERSION}-assembly.jar"
cp "${IVY_MODULE_DIR}/poms/starlake-streaming_2.13.pom" \
  "${STAGING_DIR}/starlake-streaming_2.13-${VERSION}.pom"
cp "${IVY_MODULE_DIR}/ivys/ivy.xml" \
  "${STAGING_DIR}/ivy-${VERSION}.xml"

echo "==> Staged assets:"
ls -la "${STAGING_DIR}"

GH_CMD=(gh release create "v${VERSION}"
  "${STAGING_DIR}/starlake-streaming_2.13-${VERSION}.jar"
  "${STAGING_DIR}/starlake-streaming_2.13-${VERSION}-assembly.jar"
  "${STAGING_DIR}/starlake-streaming_2.13-${VERSION}.pom"
  "${STAGING_DIR}/ivy-${VERSION}.xml"
  --title "v${VERSION}"
  --generate-notes)

if $RUN; then
  echo "==> Running: ${GH_CMD[*]}"
  "${GH_CMD[@]}"
else
  echo "==> Dry run. Re-run with --run to execute, or run manually:"
  printf '%q ' "${GH_CMD[@]}"
  echo
fi
