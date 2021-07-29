#!/usr/bin/env bash

set -e

cd "${0%/*}"

source common.inc.sh

function error() {
  local msg=$1
  echo "Error: $msg!" >&2
  exit 1
}

if workdir_is_not_clean; then
  error "Workdir is not clean"
fi

head_tag=$(get_head_tag)
if [[ "$head_tag" ]]; then
  error "HEAD is already tagged: $head_tag"
fi

version=$(get_version)
if [[ -z "$version" ]]; then
  error "Failed to get version from $VERSION_FILE"
fi

tag="$VERSION_TAG_PREFIX$version"
echo "Creating tag $tag ..."
# Message editor will pop up.
git tag -a "$tag"
