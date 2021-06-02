#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

output_file="$OUTPUT_DIR/data.json"

echo "Writing output to $(readlink -f "$output_file") ..."
jq --sort-keys '
  # Add Poison Blade and use $cards instead of .cards.
  (.cards + [{
    "id": 999,
    "name": "Poison Blade",
    "cost": 9,
    "combos": null
  }]) as $cards | {
  "cards": [$cards[]
  | {
    "id": .id,
    "name": .name,
    "cost": .cost,
    "combos": (if .combos != null
      then
        [.combos | split(",") | .[] | tonumber] | sort
      else
        []
      end)
  }]
}' "$CARDS_FILE" | \
# Minify output by removing unnecessary whitespace.
# We could also use short keys, but the saved bandwidth (about 1.5K saved) is not worth the reduced
# code readability.
perl -0777 -pe 's/\n *(?:(".*?":) )?/\1/g' > "$output_file"
