VERSION_FILE=src/igniteinator/constants.cljs
VERSION_TAG_PREFIX=v

function workdir_is_not_clean() {
    test "$(git status --porcelain)"
}

function get_version() {
    sed -rn '/^\(def(once)? version "[^"]+"\)$/ { s/.*"([^"]+)".*/\1/; p }' "$VERSION_FILE"
}

function get_head_tag() {
    git tag -l --contains
}
