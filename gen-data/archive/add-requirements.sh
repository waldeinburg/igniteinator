#!/usr/bin/env bash
# Add requirement metadata for randomizer

cd "${0%/*}/.." || exit 1
source common.inc.sh

jq '
# Ensure order of props
.cards |= map(
    .requires = [] |
    .["requires-types"] = [] |
    .provides = []
) |
# All Projectile, Weapon, and War Machine provides damage.
# Test intersection by seeing if substracting array have any effect.
.cards |= map(
    if (.types as $t | $t - [21,28,33] != $t) then
        .provides = [1]
    else
        .
    end
) |
# All Shield provides knock-down (except Old Wooden Shield)
.cards |= map(
    .id as $id |
    if (.types | contains([23]) and $id != 75) then
        .provides = [2]
    else
        .
    end
) |
# All Projectile requires-types Bow
.cards |= map(
    if (.types | contains([21])) then
        .["requires-types"] = [5]
    else
        .
    end
) |
# All Bow requires-types Projectile
.cards |= map(
    if (.types | contains([5])) then
        .["requires-types"] = [21]
    else
        .
    end
)
' "$DATA_FILE"
