#!/usr/bin/env bash
# Missing effect found after add-requirements and remove-empty-requirements

cd "${0%/*}/.." || exit 1
source common.inc.sh

jq '
# Non-flying mounts provides knock-down
.cards |= map(
    .types as $types |
    if ($types | contains([17])) and ($types | contains([30]) | not) then
        .["provides-effect"] += [2]
    else
        .
    end
)
' "$DATA_FILE"
