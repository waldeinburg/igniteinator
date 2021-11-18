#!/usr/bin/env bash
# Get types.

cd "${0%/*}" || exit 1
source ../common.inc.sh

tmp_img=$(mktemp --suffix=".png")
trap 'rm -f "$tmp_img"' 0

jq -r '
  .cards[] | "\(.id)@\(.name)"' "../$BASE_DATA_FILE" | while read -r data; do
  id=$(cut -d'@' -f1 <<<"$data")
  name=$(cut -d'@' -f2 <<<"$data")
  img="../$IMG_INPUT_DIR/en/$name.jpg"
  if [[ "$name" = "Dragon Potion" ]]; then
    box=3
  else
    convert "$img" -crop 134x151+3645+5151 -negate "$tmp_img"
    # psm 10: single character
    # Yes, that star gets read as "He" or "ne" by Tesseract. A neat trick!
    if tesseract --psm 10 "$tmp_img" - 2>/dev/null | grep -qE 'He|ne'; then
      box=2
    else
      box=1
    fi
  fi
    # Output JSON-object. Name is for debugging.
  echo "{\"id\":$id, \"name\":\"$name\", \"box\": $box},"
done | tee "box"
