#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

mkdir -p "$DOWNLOAD_DIR"

curl "$BASE_URL/api/cards" > "$CARDS_FILE"
curl "$BASE_URL/api/cards?lang_id=1" > "$CARDS_ENGLISH_FILE"
curl "$BASE_URL/api/cards?lang_id=2" > "$CARDS_SPANISH_FILE"
curl "$BASE_URL/api/cards?lang_id=3" > "$CARDS_FRENCH_FILE"
curl "$BASE_URL/api/cards?lang_id=4" > "$CARDS_GERMAN_FILE"
curl "$BASE_URL/api/categories/1" > "$CAT1_FILE"
curl "$BASE_URL/api/categories/2" > "$CAT2_FILE"
