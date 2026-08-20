# starlake-streaming

## Releasing

`sbt release` bumps/tags/pushes the version only (no artifact publishing). After tagging,
run `scripts/gh-release.sh <version> --run` to build the release jar/pom/ivy.xml and attach
them as assets on the matching GitHub Release — starlake-core resolves this dependency
straight from those assets.