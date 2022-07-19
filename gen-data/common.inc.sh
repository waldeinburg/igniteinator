#!/usr/bin/env bash
# shellcheck disable=SC2034
RES_DIR=../resources/public
DATA_FILE="$RES_DIR/data.json"
IMG_INPUT_DIR=images
IMG_MASK_FILE=mask.png
OUTPUT_DIR="$RES_DIR/generated"
IMG_OUTPUT_DIR="$OUTPUT_DIR/img"

set -e
