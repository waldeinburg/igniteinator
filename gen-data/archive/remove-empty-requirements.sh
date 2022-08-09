#!/usr/bin/env bash
# Remove empty requirements after analysis

cd "${0%/*}/.." || exit 1
source common.inc.sh

jq '
.cards |= map(
    if .combos == [] then
        del(.combos)
    else
        .
    end
) |
.cards |= map(
    if .requires == [] then
        del(.requires)
    else
        .
    end
) |
.cards |= map(
    if .["requires-types"] == [] then
        del(.["requires-types"])
    else
        .
    end
) |
.cards |= map(
    if .provides == [] then
        del(.provides)
    else
        .
    end
)
' "$DATA_FILE"
