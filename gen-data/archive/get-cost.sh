#!/usr/bin/env bash
# Get types.

cd "${0%/*}" || exit 1
source ../common.inc.sh

type_img=$(mktemp --suffix=".png")
types_file=$(mktemp)
trap 'rm -f "$type_img" "$types_file"' 0

jq -r '
  .cards[] |
  (if .types != [1] then "card" else "title" end) as $type |
  "\(.id)@\(.name)@\($type)"' "../$BASE_DATA_FILE" | while read -r data; do
  id=$(cut -d'@' -f1 <<<"$data")
  name=$(cut -d'@' -f2 <<<"$data")
  type=$(cut -d'@' -f3 <<<"$data")
  if [[ "$type" = "title" ]]; then
    cost="[5,7,9,11,13,15,17,20]"
  else
    img="../$IMG_INPUT_DIR/en/$name.jpg"
    convert "$img" -crop 230x171+1908+3509 -negate "$type_img"
    # psm 8: single word
    cost=$(tesseract --psm 8 "$type_img" - 2>/dev/null | \
      # Correct some cases which Tesseract gets wrong and remove junk characters.
      sed -r '
      s/\$/8/;
      s/[gQ]/9/;
      s/[^0-9]//g;
      ')
      # Output JSON-object. Name is for debugging.
    fi
    echo "{\"id\":$id, \"name\":\"$name\", \"cost\": $cost},"
done | tee "cost"
