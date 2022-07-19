#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

mkdir -p "$IMG_OUTPUT_DIR"
for l in en de es fr; do
  mkdir -p "$IMG_OUTPUT_DIR/$l"
done

# For conversion
temp_img=$(mktemp --suffix=".png")
trap 'rm -f "$temp_img"' 0

jq -r '
  .cards[] | "\(.id)@\(.name)"' "$BASE_DATA_FILE" | while read -r data; do
  id=$(cut -d'@' -f1 <<<"$data")
  name=$(cut -d'@' -f2 <<<"$data")
  for l in en de es fr; do
    f="$IMG_INPUT_DIR/$l/$name.jpg"
    out="$IMG_OUTPUT_DIR/$l/$id.png"
    echo "$f -> $out"
    # Process to temporary file temp_img, then optimize.
    # The cut lines are 7 px thick. Cropping hardest seem to give the result closest to the physical cards.
    # Notice that the offical app 430x600 px does not match poker size 2.5/3.5 perfectly. Go with 403x602 instead.
    # The mask file is made with GIMP: white image, Round Corners effect, 25px, no blur or shadow. 25px seems to match
    # the physical cards pretty well.
    # Using resize instead of scale adds about 2.5 seconds the processing time per image, resulting in a processing time
    # of almost 45. But we only have to run it once and go with the potential better quality.
    convert "$f" -crop 3744x5244+151+151 -resize 430 "$IMG_MASK_FILE" -compose copy-opacity -composite "$temp_img"
    # This conversion by optipng saves about 6MB for the whole dataset compared to the official app. After switching to
    # converting from originals there's less benefit, though.
    # Converting to JPG (ImageMagick) saves about 19MB but looses the transparent border.
    # Converting to GIF (ImageMagick) saves about 31MB but the color depth is insufficient.
    optipng -out "$out" "$temp_img"
  done
done
