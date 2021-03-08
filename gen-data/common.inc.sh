# shellcheck disable=SC2034
DATA_DIR=data
CARDS_FILE="$DATA_DIR/cards.json"
CAT1_FILE="$DATA_DIR/categories-1.json"
CAT2_FILE="$DATA_DIR/categories-2.json"
BASE_URL=http://ec2-54-219-252-233.us-west-1.compute.amazonaws.com

set -e
cd "${0%/*}"
