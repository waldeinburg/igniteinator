#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

gen_src_dir="../igniteinator/src/model/generated"
output_file="$gen_src_dir/model.js"

# Get array, then reduce to map. That way we can easily change our mind about how the raw model should look.
cards_array=$(
    jq --sort-keys '[.cards[] | {
        "id": .id,
        "name": .name,
        "cost": .cost,
        "image": .image,
        "combos": (if .combos != null
            then
                [.combos | split(",") | .[] | tonumber] | sort
            else
                []
            end)
    }]' "$CARDS_FILE"
)
cards_map=$(jq --sort-keys 'reduce .[] as $card ({}; . + ($card | {"\(.id)": $card}))' <<<"$cards_array")

keys_array=$(jq '[.cards[].id] | sort' "$CARDS_FILE")

fields_array=$(jq '.[0] | keys' <<<"$cards_array")

# We could load the cards array dynamically and calculate the data then; but for now, precalculate everything.
echo "Writing output to $(readlink -f $output_file) ..."
cat <<EOF >"$output_file"
export const keys = $keys_array;
export const cards = $cards_map;
EOF
