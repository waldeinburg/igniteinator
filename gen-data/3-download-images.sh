#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

BASE_URL="http://ec2-54-219-252-233.us-west-1.compute.amazonaws.com/uploads/"
mkdir -p "$IMG_DOWNLOAD_DIR"

jq -r '.cards[] | "\(.id)@\(.name)@\(.image)"' "$CARDS_FILE" | while read -r data; do
  id=$(cut -d'@' -f1 <<<"$data")
  name=$(cut -d'@' -f2 <<<"$data")
  img=$(cut -d'@' -f3 <<<"$data" | sed 's/ /%20/g')
  curl -fo "$IMG_DOWNLOAD_DIR/$id-$name.png" "$BASE_URL$img"
done

# The missing card
curl -fo "$IMG_DOWNLOAD_DIR/999-Poison Blade.png" "$BASE_URL/cards/Base_Freeze/Poison%20Blade.png"

number_of_cards=$(jq '.cards | length' "$CARDS_FILE")
total_number_of_cards=$((number_of_cards + 1))
downloaded_images=$(find "$IMG_DOWNLOAD_DIR" -type f | wc -l)
if [[ "$total_number_of_cards" -eq "$downloaded_images" ]]; then
  echo "Number of downloaded images ok"
else
  echo "Number of downloaded images was $downloaded_images; expected $total_number_of_cards!"
  exit 1
fi

if [[ "$(exiftool download/images/*.png | awk '/^Image Size/ {print $4}' | uniq -u)" ]]; then
  echo "Expected all images to be the same size but this is not true"
  exit 1
fi
