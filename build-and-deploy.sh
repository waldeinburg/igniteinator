#!/usr/bin/env bash

set -e

cd "${0%/*}"

source common.inc.sh
# Sets REMOTE_HOST, REMOTE_DIR, and REMOTE_TEST_DIR.
source private.inc.sh

BUILD_DIR=target
SRC_DIR="$BUILD_DIR/final"
MAIN_FILE="$SRC_DIR/main.js"

TEST=
CLEAR_TEST=
CLEAN=1
BUILD_CODE=1
BUILD_SW=1
BUILD_STATIC=1
DEPLOY=1
REQUIRE_TAG=1
DEPLOY_ARGS=()

function usage_and_exit() {
    echo "Usage: $0 [--no-tag] [--test] [--clear-test] [--no-clean] [--no-build] [--no-build-sw] [--build-static-only] [--no-deploy] [--dry-deploy] [--deploy-overwrite-all]"
    echo "--no-build and --static-only implies --no-clean."
  exit "${1:-1}"
}

function append_deploy_arg() {
    DEPLOY_ARGS[${#DEPLOY_ARGS[@]}]=$1
}

TEMP=$(getopt -o '' -l 'no-tag,test,clear-test,no-clean,no-build,no-build-sw,build-static-only,no-deploy,dry-deploy,deploy-overwrite-all' -- "$@") || usage_and_exit 99
eval set -- "$TEMP"
unset TEMP
while :; do
  case "$1" in
  --no-tag)
    REQUIRE_TAG=
    shift
    ;;
  --test)
    TEST=1
    REQUIRE_TAG=
    REMOTE_DIR=$REMOTE_TEST_DIR
    shift
    ;;
  --clear-test)
    CLEAR_TEST=1
    REMOTE_DIR=$REMOTE_TEST_DIR
    REQUIRE_TAG=
    CLEAN=
    BUILD_CODE=
    BUILD_STATIC=
    shift
    ;;
  --no-clean)
    CLEAN=
    shift
    ;;
  --no-build)
    CLEAN=
    BUILD_CODE=
    BUILD_STATIC=
    shift
    ;;
  --no-build-sw)
    BUILD_SW=
    shift
    ;;
  --build-static-only)
    CLEAN=
    BUILD_CODE=
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

function build_code() {
  lein fig:build
  if [[ "$BUILD_SW" ]]; then
    lein fig:build-sw
  fi
  # Nasty hack to solve goog.global not being set correctly before being used by third party code.
  # https://clojurians.slack.com/archives/C03S1L9DN/p1664662511267109
  perl -p -i -e 's/(?<!=)=this\|\|self/=self/' "$MAIN_FILE"
}

function build_static() {
  # dotglob in subshell.
  (
  shopt -s dotglob
  cp -ra resources/public/* "$SRC_DIR"
  # Minify data.json
  jq -c . "$SRC_DIR/data.json" > "$BUILD_DIR/data.json"
  mv "$BUILD_DIR/data.json" "$SRC_DIR/data.json"
  )
}

function clear_test() {
  # Not with lein clean; support clearing while doing development.
  rm -rf "$SRC_DIR"
  mkdir -p "$SRC_DIR"
  echo "Nothing to see here." > "$SRC_DIR/index.html"
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
if [[ "$BUILD_CODE" ]]; then
  echo "Building code ..."
  build_code
fi
if [[ "$BUILD_STATIC" ]]; then
  echo "Building static ..."
  build_static
fi
if [[ "$CLEAR_TEST" ]]; then
  echo "Building test clearing ..."
  clear_test
fi
if [[ "$DEPLOY" ]]; then
  if [[ "$TEST" ]]; then
    echo "Deploying to test"
  else
    echo "Deploying ..."
  fi
  deploy
fi
