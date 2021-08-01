#!/usr/bin/env bash

set -e

SRC_DIR=target/final
SCRIPT_SUBDIR=cljs-out/main
SCRIPT_SRC="target/public/$SCRIPT_SUBDIR/main_bundle.js"
SCRIPT_DIR="$SRC_DIR/$SCRIPT_SUBDIR"

CLEAN=1
BUILD=1
DEPLOY=1
REQUIRE_TAG=1
DEPLOY_ARGS=()

function usage_and_exit() {
    echo "Usage: $0 [--no-tag] [--no-clean] [--no-build] [--no-deploy] [--dry-deploy] [--deploy-overwrite-all]"
    echo "--no-build implies --no-clean."
  exit "${1:-1}"
}

function append_deploy_arg() {
    DEPLOY_ARGS[${#DEPLOY_ARGS[@]}]=$1
}

TEMP=$(getopt -o '' -l 'no-tag,no-clean,no-build,no-deploy,dry-deploy,deploy-overwrite-all' -- "$@") || usage_and_exit 99
eval set -- "$TEMP"
unset TEMP
while :; do
  case "$1" in
  --no-tag)
    REQUIRE_TAG=
    shift
    ;;
  --no-clean)
    CLEAN=
    shift
    ;;
  --no-build)
    CLEAN=
    BUILD=
    shift
    ;;
  --no-deploy)
    DEPLOY=
    shift
    ;;
  --dry-deploy)
    append_deploy_arg --dry
    shift
    ;;
  --deploy-overwrite-all)
    append_deploy_arg --overwrite-all
    shift
    ;;
  --)
    shift
    break
    ;;
  *)
    echo "Error!" >&2
    exit 1
    ;;
  esac
done
[[ $# -eq 0 ]] || usage_and_exit

cd "${0%/*}"

source common.inc.sh
# Sets REMOTE_HOST and REMOTE_DIR
source private.inc.sh

function tag_error() {
  local msg=$1
  echo "Error: $msg! Use --no-tag to ignore." >&2
  exit 1
}

function check_tag() {
  local version
  local head_tag
  if workdir_is_not_clean; then
    tag_error "Workdir is not clean"
  fi
  head_tag=$(get_head_tag)
  if [[ -z "$head_tag" ]]; then
    tag_error "HEAD is not tagged"
  fi
  version=$(get_version)
  if [[ "$head_tag" != "$VERSION_TAG_PREFIX$version" ]]; then
    tag_error "HEAD tag $head_tag does not match version $version in $VERSION_FILE"
  fi
}

function clean() {
  lein clean
}

function build() {
  lein fig:build
  # With advanced optimizations we can leave out all the other files.
  mkdir -p "$SCRIPT_DIR"
  mv "$SCRIPT_SRC" "$SCRIPT_DIR"
  # dotglob in subshell.
  (
  shopt -s dotglob
  cp -ra resources/public/* "$SRC_DIR"
  )
}

function deploy() {
  # Using https://github.com/waldeinburg/poor-mans-rsync
  ./remote_sync.sh "${DEPLOY_ARGS[@]}" "$SRC_DIR" "$REMOTE_HOST" "$REMOTE_DIR"
}

if [[ "$REQUIRE_TAG" ]]; then
  echo "Checking tag ..."
  check_tag
fi
if [[ "$CLEAN" ]]; then
  echo "Cleaning ..."
  clean
fi
if [[ "$BUILD" ]]; then
  echo "Building ..."
  build
fi
if [[ "$DEPLOY" ]]; then
  echo "Deploying ..."
  deploy
fi
