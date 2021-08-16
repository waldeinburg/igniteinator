#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

BASE_URL="http://ec2-54-219-252-233.us-west-1.compute.amazonaws.com/uploads/"
rm -rf "$IMG_DOWNLOAD_DIR"
mkdir -p "$IMG_DOWNLOAD_DIR"

jq -rs '
  (.[1].cards + [{
    "name": "Poison Blade",
    "image": "cards/Base_Freeze/Poison%20Blade.png"
  }]) as $items |
  .[0].cards |
  map({
    "id": .id,
    "name": .name,
    "image": (.name as $name | $items[] | select(.name==$name) | .image)
  }) |
  .[] | "\(.id)@\(.name)@\(.image)"' "$BASE_DATA_FILE" "$CARDS_FILE" | while read -r data; do
  id=$(cut -d'@' -f1 <<<"$data")
  name=$(cut -d'@' -f2 <<<"$data")
  img=$(cut -d'@' -f3 <<<"$data" | sed 's/ /%20/g')
  curl -fo "$IMG_DOWNLOAD_DIR/$id-$name.png" "$BASE_URL$img"
done

number_of_cards=$(jq '.cards | length' "$BASE_DATA_FILE")
downloaded_images=$(find "$IMG_DOWNLOAD_DIR" -type f | wc -l)
if [[ "$number_of_cards" -eq "$downloaded_images" ]]; then
  echo "Number of downloaded images ok"
else
  echo "Number of downloaded images was $downloaded_images; expected $number_of_cards!"
  exit 1
fi

if [[ "$(exiftool download/images/*.png | awk '/^Image Size/ {print $4}' | uniq -u)" ]]; then
  echo "Expected all images to be the same size but this is not true"
  exit 1
fi
