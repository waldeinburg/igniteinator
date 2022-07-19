#!/usr/bin/env bash
# Take generated data.json and prettify it to replace base-data.json.

cd "${0%/*}/.." || exit 1
source common.inc.sh

jq '
(.setups | map(
    {
        "id": .id,
        "name": .name,
        "requires": .requires,
        "cards": .cards
    }
)) as $setups |
(.cards | map(
    {
        "id": .id,
        "name": .name,
        "box": .box,
        "ks": .ks,
        "count": .count,
        "cost": .cost,
        "honor": .honor,
        "types": .types,
        "combos": .combos,
    }
)) as $cards |
{
    "boxes": .boxes,
    "types": .types,
    "setups": $setups,
    "combos": .combos,
    "cards": $cards
} |
del(.. | nulls)  # Remove all nulls
' "$JSON_OUTPUT_FILE" > "$DATA_FILE"

if ! diff -C5 <(jq -S . "$JSON_OUTPUT_FILE") <(jq -S . "$DATA_FILE"); then
    echo "Data differs (differences above)!"
    exit 1
fi
