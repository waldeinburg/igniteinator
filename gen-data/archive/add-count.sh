#!/usr/bin/env bash
# Add counts
cd "${0%/*}" || exit 1
source ../common.inc.sh

jq '
.cards |=
map(
    .types as $types |
    .name as $name |
    .count = (
        if ($types | index(1)) then
            1
        elif $name == "March" then
            41
        elif $name == "Dagger" then
            33
        elif $name == "Old Wooden Shield" then
            25
        elif $name == "Arrow" then
            20
        else
            10
        end
    )
)
' "../$BASE_DATA_FILE"