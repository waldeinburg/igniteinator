#!/usr/bin/env bash
# Generate basic data-set with new id's.
# This makes us robust to unwanted changes on the server.

cd "${0%/*}" || exit 1
source ../common.inc.sh

jq '
  # Add Poison Blade missing from data.
  (.cards + [{
  "name":"Poison Blade"
  }]) |
  sort_by(.name) |
  to_entries |
  map({
    "id": (.key + 1),
    "name": .value.name
  })
' "../$CARDS_FILE"
