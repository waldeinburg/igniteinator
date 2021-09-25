#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

echo "Writing output to $(readlink -f "$JSON_OUTPUT_FILE") ..."
jq --slurp --compact-output --sort-keys '
  .[0].cards as $cards |
  # Map id and combos on downloaded set. We only use combos from the original set.
  (
    (.[1].cards +
    # Add Poison Blade or the card will be lost in the final mapping.
    [{
      "id": -42, # Just a temporary id; avoid clash
      "name": "Poison Blade"
    }]) |
    # First map id an split combos into number array.
    map(.name as $name | {
      "id": ($cards[] | select(.name == $name) | .id),
      "orgId": .id,
      "combos": (if .combos != null
        then
          [.combos | split(",") | .[] | tonumber] | sort
        else
          []
        end)
    }) as $tmp_combo_cards |
    $tmp_combo_cards |
    # Then map combo ids to real id.
    map(.combos = (.combos | map(. as $id |
      ($tmp_combo_cards[] | select(.orgId == $id)).id
    ))) as $combo_cards_base |
    $combo_cards_base |
    # But the combos should go both ways which they do not. Calculate now instead of in app.
    map(.id as $id | .combos += [
      $combo_cards_base[] | select(.combos | any(. == $id)) | .id
    ]) |
    # Ensure unique (and sorted) ids
    map(.combos |= unique)
  ) as $combo_cards |
  # Merge combos.
  .[0] |
  .cards = (.cards | map(.id as $id | .combos =
    ($combo_cards[] | select(.id == $id)).combos))
' "$BASE_DATA_FILE" "$CARDS_FILE" > "$JSON_OUTPUT_FILE"

cards_in_data=$(jq '.cards | length' "$BASE_DATA_FILE")
cards_in_output=$(jq '.cards | length' "$JSON_OUTPUT_FILE")
if [[ "$cards_in_output" -ne "$cards_in_data" ]]; then
  echo "Expected $cards_in_data cards in output but got $cards_in_output!"
  exit 1
fi
