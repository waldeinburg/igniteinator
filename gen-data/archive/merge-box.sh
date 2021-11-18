#!/usr/bin/env bash
# Use types file from get-types.sh to merge types into base-data.json.

cd "${0%/*}" || exit 1
source ../common.inc.sh

box=$(echo '['; head -n-1 box; tail -n1 box | sed -r 's/,$//'; echo ']')

jq --slurp '
.[0] as $box_data |
.[1] |
.cards |= (
map(
  .id as $id |
  .box = ($box_data[] | select(.id == $id) | .box)
) |
map(
  {
    id: .id,
    name: .name,
    box: .box,
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
' <(echo "$box") "../$BASE_DATA_FILE"
