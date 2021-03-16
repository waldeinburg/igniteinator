#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

curl "$BASE_URL/api/cards" > "$CARDS_FILE"
curl "$BASE_URL/api/categories/1" > "$CAT1_FILE"
curl "$BASE_URL/api/categories/2" > "$CAT2_FILE"
