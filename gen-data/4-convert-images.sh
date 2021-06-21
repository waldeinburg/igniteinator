#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

dir="$IMG_OUTPUT_DIR_ENGLISH"
mkdir -p "$dir"

find "$IMG_DOWNLOAD_DIR" -type f | while read -r f; do
  # filename: <id>-<name>.png
  basename=${f##*/}
  id=${basename%%-*}
  # The compression in the originals is inefficient.
  # This conversion by optipng saves about 6MB for the whole dataset.
  # Converting to JPG (ImageMagick) saves about 19MB but looses the transparent border.
  # Converting to GIF (ImageMagick) saves about 31MB but the color depth is insufficient.
  out="$dir/$id.png"
  echo "$f -> $out"
  optipng -out "$out" "$f"
done
