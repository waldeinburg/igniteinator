#!/usr/bin/env bash
# Get types.

cd "${0%/*}" || exit 1
source ../common.inc.sh

tmp_img=$(mktemp --suffix=".png")
trap 'rm -f "$tmp_img"' 0

jq -r '
  .cards[] |
  (if .types != [1] then "card" else "title" end) as $type |
  "\(.id)@\(.name)@\($type)"' "../$BASE_DATA_FILE" | while read -r data; do
  id=$(cut -d'@' -f1 <<<"$data")
  name=$(cut -d'@' -f2 <<<"$data")
  img="../$IMG_INPUT_DIR/en/$name.jpg"
  type=$(cut -d'@' -f3 <<<"$data")
  if [[ "$type" = "title" ]]; then
    honor=null
  elif [[ "$name" = "Dragon Potion" ]]; then
    # This one fails.
    honor=1
  else
    convert "$img" -crop 237x278+328+316 -negate "$tmp_img"
    # psm 8: single word
    honor=$(tesseract --psm 8 "$tmp_img" - 2>/dev/null | \
      # Correct some cases which Tesseract gets wrong and remove junk characters.
      sed -r '
      s/Z/2/;
      s/7/1/;
      s/[^0-9]//g;
      ')
  fi
    # Output JSON-object. Name is for debugging.
  echo "{\"id\":$id, \"name\":\"$name\", \"honor\": $honor},"
done | tee "honor"
