#!/usr/bin/env bash
# Show commit log since last tag.

set -e

cd "${0%/*}"

git log "$(git describe --abbrev=0)..HEAD"
