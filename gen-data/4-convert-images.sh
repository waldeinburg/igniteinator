#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

dir="$IMG_OUTPUT_DIR_ENGLISH"
mkdir -p "$dir"

find "$IMG_DOWNLOAD_DIR" -type f | while read -r f; do
  # filename: <id>-<name>.png
  basename=${f##*/}
  id=${basename%%-*}
  out="$dir/$id.jpg"
  echo "$f -> $out"
  convert "$f" "$out"
done
