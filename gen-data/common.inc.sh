#!/usr/bin/env bash
# shellcheck disable=SC2034
BASE_DATA_FILE=base-data.json
DOWNLOAD_DIR=download
IMG_DOWNLOAD_DIR="$DOWNLOAD_DIR/images"
CARDS_FILE="$DOWNLOAD_DIR/cards.json"
CARDS_ENGLISH_FILE="$DOWNLOAD_DIR/cards-english.json"
CARDS_SPANISH_FILE="$DOWNLOAD_DIR/cards-spanish.json"
CARDS_FRENCH_FILE="$DOWNLOAD_DIR/cards-french.json"
CARDS_GERMAN_FILE="$DOWNLOAD_DIR/cards-german.json"
CAT1_FILE="$DOWNLOAD_DIR/categories-1.json"
CAT2_FILE="$DOWNLOAD_DIR/categories-2.json"
BASE_URL=http://ec2-54-219-252-233.us-west-1.compute.amazonaws.com
OUTPUT_DIR=../igniteinator/resources/public/generated
IMG_OUTPUT_DIR="$OUTPUT_DIR/img"
IMG_OUTPUT_DIR_ENGLISH="$IMG_OUTPUT_DIR/en"

set -e
