#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

find "$IMG_DOWNLOAD_DIR" -type f | while read -r f; do
  basename "$f"
  convert "$f" -crop 220x25+105+535 - | tesseract - -
done
