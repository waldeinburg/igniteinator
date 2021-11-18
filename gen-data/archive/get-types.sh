#!/usr/bin/env bash
# Get types.

cd "${0%/*}" || exit 1
source ../common.inc.sh

tmp_img=$(mktemp --suffix=".png")
trap 'rm -f "$tmp_img"' 0

jq -r '.cards[] | "\(.id)@\(.name)"' "../$BASE_DATA_FILE" | while read -r data; do
  id=$(cut -d'@' -f1 <<<"$data")
  name=$(cut -d'@' -f2 <<<"$data")
  img="../$IMG_INPUT_DIR/en/$name.jpg"
  # Hatchet is widest.
  # Arrow with the short word "Projectile" gives the height but some images fail without a little extra margin.
  convert "$img" -crop 2091x224+974+4998 "$tmp_img"
  # psm 7: single text line
  types=$(tesseract --psm 7 "$tmp_img" - 2>/dev/null | \
    # Remove junk characters, trim, replace the delimiter with comma, and quote.
    # The whitespace around the dash might not have been detected.
    # Tesseract gets some words wrong. Using --user-words with jq -r '.types[] | .name' does nothing.
    sed -r '
    s/[^A-Za-z -]//g;
    s/(^|[ -])[A-Za-z]{1,2}($|[ -])//g;
    s/ +/ /g;
    s/^[ -]+|[- ]+$//g;
    s/ ?- ?/,/g;
    s/(,?)([^,]+)(,?)/\1"\2"\3/g;
    s/AOE/AoE/;
    s/spell/Spell/;
    s/Ttem/Item/;
    s/Tite/Title/;
    ')
    # Output JSON-object. Name is for debugging.
    echo "{\"id\":$id, \"name\":\"$name\", \"types\": [$types]},"
done | tee "types"
