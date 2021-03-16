#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

BASIC_DATA_QUERY='{"id":.id, "name":.name}'
SORT_QUERY="sort_by(.id)"

function assert_no_diff() {
    local a=$1
    local b=$2
    if ! diff <(echo "$a") <(echo "$b"); then
        echo "Data differs (differences above)!"
        exit 1
    fi
}

echo "Verify that categories 1 data are the same in categories 2"
cat_basic_data_query='{"id":.id, "name":.name, "cards":[.cards[] | '"$BASIC_DATA_QUERY"'] | '"$SORT_QUERY"'}] | '"$SORT_QUERY"
cat1_ids=$(jq '[.data[].id]' "$CAT1_FILE")
cat1_data=$(jq --sort-keys "[.data[] | $cat_basic_data_query" "$CAT1_FILE")
cat2_base_only_data=$(jq --sort-keys "[.data[] | .id as \$cur_id | select($cat1_ids | index(\$cur_id)) | $cat_basic_data_query" "$CAT2_FILE")
assert_no_diff  "$cat1_data" "$cat2_base_only_data"

echo "The cards with combos in the cards list is the same as the allcombocards list"
basic_and_combos_query='{"id":.id, "name":.name, "combos":.combos}'
allcombocards=$(jq "[.allcombocards[] | $basic_and_combos_query] | $SORT_QUERY" "$CARDS_FILE")
cards_with_combos=$(jq "[.cards[] | select(.combos!=null) | $basic_and_combos_query] | $SORT_QUERY" "$CARDS_FILE")
assert_no_diff "$allcombocards" "$cards_with_combos"
