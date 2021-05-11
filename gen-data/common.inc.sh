#!/usr/bin/env bash
# shellcheck disable=SC2034
DATA_DIR=data
DOWNLOAD_DIR=download
CARDS_FILE="$DOWNLOAD_DIR/cards.json"
CARDS_ENGLISH_FILE="$DOWNLOAD_DIR/cards-english.json"
CARDS_SPANISH_FILE="$DOWNLOAD_DIR/cards-spanish.json"
CARDS_FRENCH_FILE="$DOWNLOAD_DIR/cards-french.json"
CARDS_GERMAN_FILE="$DOWNLOAD_DIR/cards-german.json"
CAT1_FILE="$DOWNLOAD_DIR/categories-1.json"
CAT2_FILE="$DOWNLOAD_DIR/categories-2.json"
BASE_URL=http://ec2-54-219-252-233.us-west-1.compute.amazonaws.com

set -e
