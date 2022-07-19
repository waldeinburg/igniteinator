#!/usr/bin/env bash
# shellcheck disable=SC2034
BASE_DATA_FILE=base-data.json
DATA_FILE=../resources/public/data.json
DOWNLOAD_DIR=download
IMG_INPUT_DIR=images
CARDS_FILE="$DOWNLOAD_DIR/cards.json"
CARDS_ENGLISH_FILE="$DOWNLOAD_DIR/cards-english.json"
CARDS_SPANISH_FILE="$DOWNLOAD_DIR/cards-spanish.json"
CARDS_FRENCH_FILE="$DOWNLOAD_DIR/cards-french.json"
CARDS_GERMAN_FILE="$DOWNLOAD_DIR/cards-german.json"
CAT1_FILE="$DOWNLOAD_DIR/categories-1.json"
CAT2_FILE="$DOWNLOAD_DIR/categories-2.json"
BASE_URL=http://ec2-54-219-252-233.us-west-1.compute.amazonaws.com
IMG_MASK_FILE=mask.png
OUTPUT_DIR=../resources/public/generated
JSON_OUTPUT_FILE="$OUTPUT_DIR/data.json"
IMG_OUTPUT_DIR="$OUTPUT_DIR/img"

set -e
