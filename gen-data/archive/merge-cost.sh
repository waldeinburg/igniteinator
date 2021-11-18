#!/usr/bin/env bash
# Use types file from get-types.sh to merge types into base-data.json.

cd "${0%/*}" || exit 1
source ../common.inc.sh

cost=$(echo '['; head -n-1 cost; tail -n1 cost | sed -r 's/,$//'; echo ']')

jq --slurp '
.[0] as $cost_data |
.[1] |
.cards |=
map(
  .id as $id |
  .cost = ($cost_data[] | select(.id == $id) | .cost)
) |
# Rearrange keys just to be pedantic.
.cards |=
map(
  {
    id: .id,
    name: .name,
    cost: .cost,
    types: .types
  }
)
' <(echo "$cost") "../$BASE_DATA_FILE"
