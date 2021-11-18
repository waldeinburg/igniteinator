#!/usr/bin/env bash
# Use types file from get-types.sh to merge types into base-data.json.

cd "${0%/*}" || exit 1
source ../common.inc.sh

honor=$(echo '['; head -n-1 honor; tail -n1 honor | sed -r 's/,$//'; echo ']')

jq --slurp '
.[0] as $honor_data |
.[1] |
.cards |= (
map(
  .id as $id |
  .honor = ($honor_data[] | select(.id == $id) | .honor)
) |
# Rearrange keys just to be pedantic.
map(
  {
    id: .id,
    name: .name,
    cost: .cost,
    honor: .honor,
    types: .types
  }
) |
map(
  if .honor == null then
    del(.honor)
  else
    .
  end
))
' <(echo "$honor") "../$BASE_DATA_FILE"
