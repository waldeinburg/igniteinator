#!/usr/bin/env bash
cd "${0%/*}" || exit 1
source common.inc.sh

BASIC_DATA_QUERY='{"id":.id, "name":.name}'
SORT_QUERY="sort_by(.id)"

function test_diff() {
    local a=$1
    local b=$2
    if ! diff -C5 <(echo "$a") <(echo "$b"); then
        echo "Data differs (differences above)!"
        return 1
    fi
}

function assert_no_diff() {
  test_diff "$1" "$2" || exit 1
}

function warn_on_diff() {
  test_diff "$1" "$2" || return 0 # set -e is active
}

echo "Verify that name and image does not contain suspect stuff"
if jq '.cards[] | [.name, .image]' "$CARDS_FILE" | grep -C10 '@'; then
  echo "@ in name or image found! This would break image download script"
  exit 1
fi
# Prevent data from stepping out of the download folder.
if jq '.cards[] | .name' "$CARDS_FILE" | grep -FC10 '..'; then
  echo ".. in name found! This could make download script dangerous!"
  exit 1
fi

echo "Verify that the default cards file and id set to English are the same"
assert_no_diff "$(jq . "$CARDS_FILE")" "$(jq . "$CARDS_ENGLISH_FILE")"

echo "Verify that the number of cards are the same for all languages"
function cards_no() {
    jq '.cards | length' "$1"
}
cards_no_english=$(jq '.cards | length' "$CARDS_FILE")
for l in "$CARDS_SPANISH_FILE" "$CARDS_FRENCH_FILE" "$CARDS_GERMAN_FILE"; do
    echo "Verify for $l"
    assert_no_diff "$cards_no_english" "$(cards_no "$CARDS_SPANISH_FILE")"
done

echo "Verify that all languages have the same combos"
base_list=$(jq '.cards as $cards |
    [.cards[] | {
        "name": .name,
        "combos":
            (if .combos != null
                then
                    [.combos | split(",") | .[] | tonumber] as $combos |
                    $cards | map(.id as $id | select($combos | index($id))) |
                    [.[] | .name] | sort
                else
                    []
                end)
    }] | sort_by(.name)' "$CARDS_FILE")
for l in "$CARDS_SPANISH_FILE" "$CARDS_FRENCH_FILE" "$CARDS_GERMAN_FILE"; do
    echo "Verify for $l"
    this_list=$(jq '
    [.cards[] | {
        "id": .id,
        "name": (.image | split("/")[-1] | split(".")[0]),
        "combos": .combos
    }] as $cards |
    [$cards[] | {
        "name": .name,
        "combos":
            (if .combos != null
                then
                    [.combos | split(",") | .[] | tonumber] as $combos |
                    $cards | map(.id as $id | select($combos | index($id))) |
                    [.[] | .name] | sort
                else
                    []
                end)
    }] | sort_by(.name)' "$l")
    # At the time of writing there are errors but we don't care.
    warn_on_diff "$base_list" "$this_list"
done

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

echo "Verify that data set is not different from base data set"
# This also verifies that Poison Blade is still missing.
cards_names=$(jq '(.cards + [{"name":"Poison Blade"}]) | sort_by(.name) | map(.name)' "$CARDS_FILE")
base_data_names=$(jq '.cards | map(.name)' "$BASE_DATA_FILE")
assert_no_diff "$cards_names" "$base_data_names"
