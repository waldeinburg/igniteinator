#!/usr/bin/env bash
# Use types file from get-types.sh to merge types into base-data.json.

cd "${0%/*}" || exit 1
source ../common.inc.sh

types=$(echo '['; head -n-1 types; tail -n1 types | sed -r 's/,$//'; echo ']')

jq --slurp '
.[1].types as $types |
(
  .[0] |
  map(
    .types as $org_types |
    .types |= map(
      . as $name | $types[] | select(.name == $name) | .id
    ) |
    if ($org_types | length) == (.types | length) then
      .
    else
      error("Error mapping type ids for type \(.id) (\(.name))")
    end
  )
) as $type_data |

.[1] |
.cards |=
map(
  .id as $id |
  .types = ($type_data[] | select(.id == $id) | .types)
)
' <(echo "$types") "../$BASE_DATA_FILE"
